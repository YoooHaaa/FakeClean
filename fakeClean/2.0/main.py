import threading
import subprocess
import time
import sys
import os
import sys
import time
import click
import datetime
import threading
import time

global_time = time.time()
threadLock = threading.Lock()

#*************************************************************************************************************
class Show(object):
    # blue  green   white  red   yellow
    @classmethod
    def error(cls, func, err):
        click.secho('%-20s%-20s%-20s' %('[error]', func, err), fg='red')
        with open("err.txt", "a", encoding='utf-8') as log:
            log.write("********************************************************************************\n")
            log.write(str(datetime.datetime.now()))
            log.write("\n")
            log.write(func)
            log.write("\n")
            log.write(err)
            log.write("\n")

    @classmethod
    def warning(cls, war):
        click.secho('%-20s%-20s' %('[warning]', war), fg='blue')

    @classmethod
    def info(cls, inf):
        click.secho('%-20s%-20s' %('[info]', inf), fg='white')

    @classmethod
    def user(cls, title, info, change, color):
         click.secho('%-20s%-20s%-20s' %(title, info, change), fg=color)


#*************************************************************************************************************
class CheckLive(threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        


    def run(self):
        '''
        function: 不间断读取日志，并筛选出文件操作相关信息并打印出来
        '''
        time.sleep(3)
        while True:
            try:
                self.excute_shell_single('adb push test /sdcard/yooha/test')
                time.sleep(3)
                self.excute_shell_multiple(['su\n', 'cd /sdcard/yooha\n', 'rm test\n', 'exit\n'])
                time.sleep(1)

                # 线程同步
                threadLock.acquire()
                interval = time.time() - global_time
                threadLock.release()

                if interval > 20:
                    if interval > 50:
                        Show.warning('[Monitor]丢失信号时间过长，请重开脚本')
                        self.release()
                        os._exit(0) # 用于线程中退出
                    Show.warning('[Monitor]已丢失信号超过 ' + str(interval) + '秒，请点击悬浮球，重开文件监控服务')

            except Exception as err:
                Show.error('CheckLive -> run', str(err))


    def excute_shell_multiple(cls, cmd:list):
        '''
        function:  执行adb多条命令
        '''
        try:
            obj = subprocess.Popen(['adb', 'shell'], shell = True, stdin=subprocess.PIPE, stdout=subprocess.PIPE ,stderr=subprocess.PIPE)
            for line in cmd:
                obj.stdin.write(line.encode('utf-8'))
            info,err = obj.communicate() # 必须要有这行读取结果 不然会出错
        except Exception as err:
            Show.error('CheckLive -> excute_shell_multiple', str(err))


    def excute_shell_single(self, cmd:str):
        '''
        function:  执行adb单条命令
        '''
        try:
            obj = subprocess.Popen(cmd, shell = True, stdin=subprocess.PIPE, stdout=subprocess.PIPE ,stderr=subprocess.PIPE)
            info,err = obj.communicate() # 必须要有这行读取结果 不然会出错
        except Exception as err:
            Show.error('CheckLive -> excute_shell_single', str(err))


    def release(self):
        '''
        function: 删除临时文件
        '''
        try:
            os.remove('tmp')
            os.remove('test')
        except Exception as err:
            Show.error('InputThread -> release', str(err))
#*************************************************************************************************************
class FileThread (threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.time_previous = time.time()
        self.init()
        


    def init(self):
        '''
        function: 清理日志缓存并重新设置日志缓存大小
        '''
        try:
            os.system('adb logcat -c -b main -b events -b radio -b system')
            time.sleep(2)
            os.system('adb logcat -G 10m')
        except Exception as err:
            Show.error('FileThread -> init', str(err))


    def run(self):
        '''
        function: 不间断读取日志，并筛选除文件操作相关信息并打印出来
        '''
        try:
            obj = subprocess.Popen('adb logcat -v time', shell = True, stdin=subprocess.PIPE, stdout=subprocess.PIPE ,stderr=subprocess.PIPE)
            for item in iter(obj.stdout.readline, 'b'):
                if str(item).find("yooha-monitor") != -1:
                    str_line = str(item)
                    #print(str_line)
                    str_line = str_line.split('):')[1]
                    if not self.check(str_line):
                        Show.user('FileMonitor', str_line.split('\\r\\n')[0], '', 'green')
        except Exception as err:
            Show.error('FileThread -> run ', str(err))


    def check(self, line):
        if line.find('DELETE:/storage/emulated/0/yooha/test') != -1:
            #print("in delete test... ... ")
            threadLock.acquire()
            global global_time
            global_time = time.time()
            threadLock.release()
            return True
        return False

#*************************************************************************************************************
class ProcessThread (threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.list_complete_match = ['zygote', 'cmd', 'system', 'rm', 'ps', 'app_process', 'sh', 'su', 'xargs']
        self.list_partial_match = ['kworker/', 'miui','xiaomi','huawei', 'vivo', 'oppo', 'magiskd']
        self.list_old_process = []
        self.list_current_process = []
        self.list_delete = []

    def run(self):
        '''
        function: 不间断读取进程信息，并筛选出当前退出的进程信息并打印出来
        '''
        while True:
            time.sleep(0.3)
            info = self.get_shell('adb shell ps -A')
            self.list_current_process = []
            self.list_delete = []
            for line in info:
                linestr = str(line)
                list_line = linestr.split(' ')
                list_new = []
                for process in list_line:
                    if (process != ''):
                        list_new.append(process)
                if (list_new[1] not in self.list_current_process):
                    self.list_current_process.append({"pid":list_new[1], "process":list_new[-1].split("\\r\\n")[0]})

            if self.list_old_process:
                for old in self.list_old_process:
                    if old not in self.list_current_process:
                        self.list_delete.append(old)
            
            self.list_old_process = self.list_current_process

            if (self.list_delete):
                self.parse_data(self.list_delete)


    def parse_data(self, data):
        '''
        function:  解析退出进程列表数据
        '''
        for info in data:
            bool_out = False
            if info['process'] in self.list_complete_match:
                bool_out = True
            else:
                for out in self.list_partial_match:
                    if (info['process'].find(out) != -1):
                        bool_out = True
                        break
            if not bool_out:
                Show.user('ProcessMonitor', 'Terminate procedure : ' + info['process'], '', 'yellow')


    def get_shell(self, cmd:str) -> list:
        '''
        function:  执行adb单条命令，并获取命令行返回值
        '''
        try:
            obj = subprocess.Popen(cmd, shell = True, stdin=subprocess.PIPE, stdout=subprocess.PIPE ,stderr=subprocess.PIPE)
        except Exception as err:
            Show.error('ProcessThread -> get_shell', str(err))
            return None
        return obj.stdout.readlines()


#*************************************************************************************************************
class InputThread (threading.Thread):
    def __init__(self):
        threading.Thread.__init__(self)
        self.dict_push = {"dir":"/sdcard/Bytedownload/", "name":['1.log']}
        self.init()


    def init(self):
        '''
        function: 创建临时文件
        '''
        try:
            with open("test", "a", encoding='utf-8') as tmp:
                tmp.write("8455")
            with open("tmp", "a", encoding='utf-8') as tmp:
                tmp.write("4190")
        except Exception as err:
            Show.error('InputThread -> init', str(err))


    def release(self):
        '''
        function: 删除临时文件
        '''
        try:
            os.remove('tmp')
            os.remove('test')
        except Exception as err:
            Show.error('InputThread -> release', str(err))


    def run(self):
        '''
        function: 不间断读取进程信息，并筛选出当前退出的进程信息并打印出来
        '''
        while True:
            if self.choose_exit('输入y结束当前测试，输入n投递垃圾文件\n'):
                Show.info('测试结束')
                self.release()
                os._exit(0) # 用于线程中退出
            else:
                continue


    def choose_exit(self, hint):
        """
        function:  获取用户输入
        """
        choose = input(hint)
        sys.stdout.flush()
        if  choose == 'y' or choose == 'Y':
            return True
        elif choose == 'n' or choose == 'N':
            self.push()
            return False
        else:
            return False


    def push(self):
        """
        function:  投递文件到手机
        """
        Show.info('正在投递垃圾文件...')
        for i in range(1, 11):
            self.excute_shell_single('adb push tmp /sdcard/ByteDownload/' + str(i) + '.apk')
            self.excute_shell_single('adb push tmp /sdcard/cache/' + str(i) + '.log')
            self.excute_shell_single('adb push tmp /storage/emulated/0/Android/data/com.tencent.mobileqq/cache/sgfsrgrg' + str(i))
            self.excute_shell_single('adb push tmp /storage/emulated/0/Android/data/com.tencent.mobileqq/Tencent/MobileQQ/DoutuRes/dui_icon/tmp' + str(i) + '.png')
            self.excute_shell_single('adb push tmp /storage/emulated/0/Tencent/MobileQQ/babyQIconRes/tmp' + str(i) + '.png')
        Show.info('投递完成...')


    def excute_shell_single(self, cmd:str):
        '''
        function:  执行adb单条命令
        '''
        try:
            obj = subprocess.Popen(cmd, shell = True, stdin=subprocess.PIPE, stdout=subprocess.PIPE ,stderr=subprocess.PIPE)
            info,err = obj.communicate() # 必须要有这行读取结果 不然会出错
        except Exception as err:
            Show.error('InputThread -> excute_shell_single', str(err))


if __name__ == "__main__":
    fileinfo = FileThread()
    fileinfo.start()
    processinfo = ProcessThread()
    processinfo.start()
    myinput = InputThread()
    myinput.start()
    mylive = CheckLive()
    mylive.start()






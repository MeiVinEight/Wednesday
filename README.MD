<div align="center">
  <h1>Wednesday</h1>

  Wednesday 一个是基于 Overflow 的 MC 机器人

  [![overflow](https://img.shields.io/badge/Overflow-1.0.8-blue?style=for-the-badge)](https://github.com/MrXiaoM/Overflow) [![mirai](https://img.shields.io/badge/mirai--core--api-2.16.0-blue?style=for-the-badge)](https://github.com/mamoe/mirai) [![Onebot 11](https://img.shields.io/badge/Onebot-11-313343?style=for-the-badge)](https://11.onebot.dev)
</div>

## 丨说明

> **注意: 本项目 <font color="#FF5555">不是</font> 一个开箱即用的工具，你需要至少部署一个可用的[OneBot11](https://onebot.dev/ecosystem)实现 (如 [LLOnebot](https://github.com/LLOneBot/LLOneBot)、[NapCat](https://github.com/NapNeko/NapCatQQ)、[Gensokyo](https://github.com/Hoshinonyaruko/Gensokyo) 等)**
> 
> **并正确开启一个 WebSocket 服务器，Wednesday 主动发起连接后才可正常使用**
> 
> 🚧该项功能目前积极开发中🚧
> 
> 🎨如果有其他想法欢迎在issues中提出🎨
> 
> ✨积极欢迎贡献代码✨

## 丨使用
> **使用Gradle运行**

1. 克隆项目到本地

   ```shell
   git clone https://github.com/MeiVinEight/Wednesday
   ```

2. 使用gradlew运行

   ```shell
   ./gradlew run
   ```

> **使用Woden运行**

1. 克隆项目到本地

   ```shell
   git clone https://github.com/MeiVinEight/Wednesday
   ```

2. 使用gradlew构建项目

   ```shell
   ./gradlew build
   ```

3. 使用Java运行Woden

   ```shell
   java -cp libs/Woden-1.0.0.jar org.mve.woden.Woden
   ```

> **运行方式对比**

| 特性            | Gradle | Woden |
|----------------|--------|--------|
| 更少的内存占用   | ❌     | ✅     |
| 热重载Kaybxg    | ❌     | ❌     |
| 热重载Wednesday | ❌     | ✅     |
| 热重载其他依赖   | ❌     | ✅     |

## 丨配置
第一次运行会在运行目录生成一个配置文件`config.json`,这里给出每项的介绍
```json5
{
  "onebot": {
    // 可选字段
    // 正向WebSocket连接地址，Wednesday会通过该地址主动向OneBot端建立WebSocket连接
    // 使用WebSocket URL格式
    "ws-forward": "ws://127.0.0.1:3001",
    // 可选字段
    // 如果在OneBot端配置了连接Token，则需要在此提供
    // 留空或不提供表示无Token
    "token": "",
    // 可选字段
    // OneBot断开链接后尝试重连次数
    // 设为0则禁用重连
    "reconnect-trys": 5
  },
  // 可选字段
  // Coffee: 一个文件代理服务, 可以将指定链接的文件下载到指定位置
  // Coffee会开启一个http服务器, 可使用/upload请求让Coffee下载文件
  "coffee": {
    // 可选字段
    // Coffee默认监听的地址
    // "0.0.0.0": 全部地址
    // "127.0.0.1": 仅本机地址
    "host": "0.0.0.0",
    // 可选字段
    // Coffee服务的端口
    "port": 83,
    // 可选字段
    // Coffee将文件下载的位置
    // 请求中指定的路径都会在此路径下
    // 例如请求中指定的路径为"/image/ico.png", 此处配置的路径为"coffee"
    // 则最终文件的路径为"./coffee/image/ico.png"
    "upload": "coffee",
    // 可选字段
    // 可以对每个服务设置不同的日志等级
    // 由于Coffee提供了系统日志记录器
    // 因此一些系统日志也会使用Coffee的日志记录器输出
    // 也许有一些系统日志是你不想看到的
    // 可以在此处设置日志等级
    // 这里的每一项都是一条针对某个系统服务设置的日志等级
    "service-log-level": {
      "javax.management.remote.rmi": "INFO",
      "jdk.event.security": "INFO",
      "sun.net.www.protocol.http.HttpURLConnection": "INFO"
    }
  },
  // 可选字段
  // 日志记录等级，可选的配置项有 (DEBUG < VERBOSE < INFO < WARNING < ERROR)
  // 大小关系表示优先级
  // 只会记录优先级大于等于该配置项的日志
  "log-level": "INFO",
  // 可选字段
  // 日志使用的语言
  // 目前仅支持中文(zh_cn)
  // 其他语言仍在开发中
  "lang": "zh_cn",
  // 可选字段
  // 所有者，拥有所有控制权限
  "owner": 0,
  // 可选字段
  // 指令前缀，以此前缀开头的消息才会被处理
  "command-prefix": "/",
  // 可选字段
  // MySQL数据库地址
  "mysql-host": "127.0.0.1",
  // 可选字段
  // MySQL数据库端口
  "mysql-port": 3306,
  // 可选字段
  // MySQL数据库用户名
  "mysql-username": "root",
  // 可选字段
  // MySQL数据库密码
  "mysql-password": "root",
  // 可选字段
  // 文件服务器地址, 可为本地路径, 需要上传的文件都会保存在指定位置
  "file-server": "."
}
```

## 丨功能

+ 查询Forge混淆映射表(目前仅1.20.1)

## 丨其他

+ 本项目仅供学习使用，请勿用于非法用途
+ **[MIT](https://github.com/MeiVinEight/Wednesday/blob/master/LICENSE)**
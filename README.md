# O-MI-Security-Model
Complementary authorization and access control modules for O-MI.

Dependencies
------------
- Java-1.7
- O-MI Node 0.2.1

Installation
--------------
- Install [Nginx](http://nginx.org/en/) and configure [reverse proxy (nginx.conf)](https://github.com/filiroman/O-MI-Security-Model/blob/master/nginx_config/nginx.conf)
- Install [O-MI Node](https://github.com/AaltoAsia/O-MI)
- Install [Apache Maven](https://maven.apache.org/)
- Download [latest zip](https://github.com/filiroman/O-MI-Security-Model/archive/master.zip) and extract it
- Execute `install.sh` inside the project's folder
- Put your Facebook App credentials inside `src/main/java/com/aaltoasia/omi/accontrol/FacebookAuth.java`
- Put administrator emails into `admin_list.txt` (one row per e-mail)
- Start `O-MI Node`
- Execute `run.sh` to run the module. use -l option to specify logging level. Possible values: `error`, `warn`, `info`.

**Note!** To use client side certificates you need to generate CA, server and client side SSL certificates. [(Tutorial)](https://gist.github.com/mtigas/952344).

Now use [http://localhost:8009/security/Login](https://localhost/security/Login) for Login and Register new users, [http://localhost:8009/security/AC](https://localhost/security/AC) for Access Control and [http://localhost:8009/html/webclient/index.html](https://localhost/html/webclient/index.html) for standard O-MI Node Webclient.


Possible problems
--------------
1. After authorization AC Webclient does not work and still redirects to the Login page.

**Solution**: Add email you are using for registration to `admin_list.txt` (one row per e-mail) and delete `OMISec.db` inside project folder, then restart the module.

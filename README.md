# Server.ktor-chat
Server for the Ktor-chat app

Backend for KtorAndroidChat app: https://github.com/realityexpander/KtorAndroidChat

## Test locally without android app

Use this WebSocket testing site for local testing. (Does not work with the deployed server)

https://www.piesocket.com/websocket-tester

Websocket local url: `ws://localhost:8082/chat-socket?username=Jimbo`

## Deployment Steps

1. Download Git Bash (only if on Windows)

2. Go to your users folder and open the `~/.ssh` folder. Then open Git Bash / Terminal there and generate a key pair:

`ssh-keygen -m PEM -t rsa -b 2048`

Give it the <keyname> (I suggest hostinger_rsa)

3. Copy the key to your server:

`ssh-copy-id -i <keyname> <user>@<host>`
 
4. Make a local `/key` folder and copy the <keyname> file to your local app dev `/key` folder.
 
 The file should look like this inside:
 
 ```
 -----BEGIN RSA PRIVATE KEY-----
MIIG4wIBAAKCAYEAxSXsSpaIlJRKF11qpQEaBwnrn5Xv4EelL/Iqpn7VBRnLdefz
QE9lHmF5oVB6Em1MEXJVzs+X6CZRSVTYGezWjmy/v/07aaiGZX1MgXjS0Q6DzKPC
 ...
 -----END RSA PRIVATE KEY-----
 ```

5. Login to your Ubuntu server via SSH:

`ssh -i <keyname> <user>@<host>`

6. Update dependencies:

`sudo apt update`

7. Install Java:

`sudo apt-get install default-jdk`

8. Open /etc/ssh/sshd_config:

`sudo nano /etc/ssh/sshd_config`

9. Put this string in there, save with Ctrl+S and exit with Ctrl+X:

`KexAlgorithms curve25519-sha256@libssh.org,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521,diffie-hellman-group-exchange-sha256,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group1-sha1`

10. Restart the sshd service:

`sudo systemctl restart sshd`

11. Create a systemd service for your Ktor server:

`sudo nano /etc/systemd/system/chat.service`

12. Paste this configuration in this service, then save with Ctrl+S and exit with Ctrl+X:
```
[Unit]
Description=Chat Service
After=network.target
StartLimitIntervalSec=10
StartLimitBurst=5

[Service]
Type=simple
Restart=always
RestartSec=1
User=root
ExecStart=sudo java -jar /root/chat/chat-server.jar

[Install]
WantedBy=multi-user.target
```

13. Launch the service:

`sudo systemctl start chat`

14. Create a symlink to automatically launch the service on boot up:

`sudo systemctl enable chat`

15. Make sure, your ports are open and you forward the traffic from the standard HTTP port to 8082:
```
iptables -t nat -I PREROUTING -p tcp --dport 80 -j REDIRECT --to-ports 8082
sudo iptables -A INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -A INPUT -p tcp --dport 8082 -j ACCEPT
```

16. Then, save your iptables rules:

`sudo apt-get install iptables-persistent`

17. Install MongoDB
```
apt-key adv --keyserver hkp://keyserver.ubuntu.com:80 --recv 9DA31620334BD75D9DCB49F368818C72E52529D4
echo "deb [ arch=amd64 ] https://repo.mongodb.org/apt/ubuntu bionic/mongodb-org/4.0 multiverse" | sudo tee /etc/apt/sources.list.d/mongodb.list
apt-get update
apt-get install mongodb-org
systemctl unmask mongod
systemctl enable mongod
systemctl restart mongod
```

18. Make sure the folder /root/chat/ exists
```
cd /root
mkdir chat
```

19. Add `environement` file in `/root/chat/`

`nano environment`

```
MONGO_USERNAME="ADMIN_USERNAME_IN_QUOTES"
MONGO_PASSWORD="PASSWORD_IN_QUOTES"
MONGO_HOST=localhost
MONGO_PORT=27017
MONGO_AUTH_SOURCE=admin
```
ctrl-s, ctrl-x

20. Deploy automatically with gradle

On local dev machine terminal:
`gradlew deploy`

# Be sure to add the user and password to the Mongo Admin database! OR IT WILL DEFINITELY BE HACKED!!!

### Create an admin user from MongoSh (mongo shell) or Mongo Compass
 ```
  use admin
  
  db.createUser({user: "ADMIN_USERNAME", pwd: "PASSWORD_RAW_NO_EXTRA_QUOTES", roles: [{role: "readWrite", db: "admin"}]})
 ```
 or this option (which you will be prompted for the password, so it wont be visible like the above step):
 ```
  db.createUser({user: "ADMIN_USERNAME", pwd: passwordPrompt(),
    roles: [
      { role: "userAdminAnyDatabase", db: "admin" },
      { role: "readWriteAnyDatabase", db: "admin" }
  ]})
  ```
 Auth the user:
 ```
  use admin
  db.auth("ADMIN_USERNAME", passwordPrompt()) // or cleartext password
  ```
 Show users:
 ```
  use admin
  db.system.users.find()
  ```
 Remove user:
 ```
  use admin
  db.system.users.deleteOne({user: "user"})
  ```


## Trouble shooting

May need to run to reset `chat.service` (on remote ubuntu server):

`systemctl daemon-reload` 

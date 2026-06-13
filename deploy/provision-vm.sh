#!/usr/bin/env bash
#
# provision-vm.sh — one-shot setup of the Oracle Cloud VM (Ubuntu 22.04)
# for the MeTA DevOps final project bonus (#5: public IP).
#
# Installs Java 21 + Tomcat 10.1, creates the team webapp folder, opens the
# OS firewall on 8080, and runs Tomcat as a systemd service owned by 'ubuntu'
# so Jenkins can scp the jsp straight in without sudo.
#
set -euo pipefail

APP="roi-shiraz-omri-noa-arbel-app"
TOMCAT_VER="10.1.55"
CATALINA="/opt/tomcat"
DEPLOY_USER="ubuntu"

echo "==> [1/6] Installing Java 21 + tools"
sudo apt-get update -y
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y openjdk-21-jdk curl

echo "==> [2/6] Downloading Tomcat ${TOMCAT_VER}"
cd /tmp
curl -fsSL -o tomcat.tar.gz \
  "https://dlcdn.apache.org/tomcat/tomcat-10/v${TOMCAT_VER}/bin/apache-tomcat-${TOMCAT_VER}.tar.gz" \
  || curl -fsSL -o tomcat.tar.gz \
  "https://archive.apache.org/dist/tomcat/tomcat-10/v${TOMCAT_VER}/bin/apache-tomcat-${TOMCAT_VER}.tar.gz"
sudo rm -rf "${CATALINA}"
sudo mkdir -p "${CATALINA}"
sudo tar -xzf tomcat.tar.gz -C "${CATALINA}" --strip-components=1

echo "==> [3/6] Creating team webapp folder: ${APP}"
sudo mkdir -p "${CATALINA}/webapps/${APP}"
echo '<%@ page contentType="text/html;charset=UTF-8" %>Provisioned. Awaiting Jenkins deploy.' \
  | sudo tee "${CATALINA}/webapps/${APP}/index.jsp" >/dev/null

echo "==> [4/6] Permissions (let '${DEPLOY_USER}' deploy without sudo)"
sudo chown -R "${DEPLOY_USER}:${DEPLOY_USER}" "${CATALINA}"

echo "==> [5/6] Opening port 8080 in the OS firewall"
# Oracle's Ubuntu images ship iptables rules that REJECT everything except SSH.
if ! sudo iptables -C INPUT -p tcp --dport 8080 -j ACCEPT 2>/dev/null; then
  sudo iptables -I INPUT -p tcp --dport 8080 -j ACCEPT
fi
sudo DEBIAN_FRONTEND=noninteractive apt-get install -y iptables-persistent || true
sudo netfilter-persistent save || true

echo "==> [6/6] Installing systemd service for Tomcat"
JAVA_HOME_PATH="$(dirname "$(dirname "$(readlink -f "$(which java)")")")"
sudo tee /etc/systemd/system/tomcat.service >/dev/null <<EOF
[Unit]
Description=Apache Tomcat 10
After=network.target

[Service]
Type=forking
User=${DEPLOY_USER}
Group=${DEPLOY_USER}
Environment=JAVA_HOME=${JAVA_HOME_PATH}
Environment=CATALINA_HOME=${CATALINA}
Environment=CATALINA_PID=${CATALINA}/temp/tomcat.pid
ExecStart=${CATALINA}/bin/startup.sh
ExecStop=${CATALINA}/bin/shutdown.sh
Restart=on-failure

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable --now tomcat
sleep 8

echo
echo "==> Smoke test"
CODE=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "http://localhost:8080/${APP}/" || echo "000")
echo "Local: http://localhost:8080/${APP}/ -> HTTP ${CODE}"
echo "Done."

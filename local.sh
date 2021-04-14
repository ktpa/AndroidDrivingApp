#!/usr/bin/env bash

if [[ ! "$1" =~ ^(start|stop) ]]; then
    echo -e "\033[1;31mInvalid argument '$1'\033[0m"
    echo -e "\033[1;37mUsage: $0 (start|stop)\033[0m"
    exit 1
fi

if [ ! -x "$(command -v docker)" ]; then
    echo -e "\033[1;31mNo Docker installation found\033[0m"
    echo ""
    echo -e "\033[1;37mInstall using:\033[0m"
    echo -e "  - \033[1;32mHomebrew\033[0m:"
    echo -e "\t\033[1;37mbrew install docker\033[0m"
    echo -e "  - \033[1;32mAPT\033[0m:"
    echo -e "\t\033[1;37msudo apt update\033[0m"
    echo -e "\t\033[1;37msudo apt install docker-ce docker-ce-cli containerd.io\033[0m"
    echo -e "\033[1;37mOr visit:\033[0m"

    case "$OSTYPE" in
      "darwin"*) echo -e "\t\033[1;36mhttps://docs.docker.com/docker-for-mac/install/\033[0m" ;;
      "linux-gnu") echo -e "\t\033[1;36mhttps://docs.docker.com/engine/install/ubuntu/\033[0m" ;;
    esac

    exit 1
fi

if [ ! -x "$(command -v docker-compose)" ]; then
    echo -e "\033[1;31mNo Docker Compose installation found\033[0m"
    echo ""
    echo -e "\033[1;37mInstall using:\033[0m"
    echo -e "  - \033[1;32mHomebrew\033[0m:"
    echo -e "\t\033[1;37mbrew install docker-compose\033[0m"
    echo -e "  - \033[1;32mpip\033[0m:"
    echo -e "\t\033[1;37mpip install docker-compose\033[0m"
    echo -e "\033[1;37mOr visit:\033[0m"
    echo -e "\t\033[1;36mhttps://docs.docker.com/compose/install/\033[0m"
    exit 1
fi

echo -e "\033[1;37mChecking for available Docker instance\033[0m"

$(curl -s --unix-socket /var/run/docker.sock http://ping > /dev/null)
docker_status=$?

if [ "$docker_status" == "7" ]; then
    echo -e "\033[1;31mNo Docker instance available\033[0m"
    echo -e "\033[1;33mRun the following command to start the Docker daemon:\033[0m"

    case "$OSTYPE" in
      "darwin"*) echo -e "    \033[1;37mopen -a Docker\033[0m" ;;
      "linux-gnu") echo -e "    \033[1;37msudo systemctl start docker\033[0m" ;;
    esac

    exit 1
fi

echo -e "\033[1;32mDocker instance found\033[0m"

case "$1" in
  "start")
    echo -e "\033[1;37mStarting local development environment\033[0m"
    $(docker-compose --log-level ERROR up -d)

    env_start_status=$?

    case "$env_start_status" in
      0) echo -e "\033[1;32mSuccessfully started local development environment\033[0m";;
      *) echo -e "\033[1;31mFailed to start local development environment ($env_start_status)\033[0m";;
    esac
    ;;
  "stop")
    echo -e "\033[1;37mStopping local development environment\033[0m"
    $(docker-compose --log-level ERROR down)

    env_stop_status=$?

    case "$env_stop_status" in
      0) echo -e "\033[1;32mSuccessfully stopped local development environment\033[0m";;
      *) echo -e "\033[1;31mFailed to stop local development environment ($env_stop_status)\033[0m";;
    esac
esac

exit 0
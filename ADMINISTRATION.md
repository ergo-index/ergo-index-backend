# Initial Setup (One-Time Only)
1. Install Docker Engine and Docker Compose (and make sure Docker is running)
2. From the project's root directory, run: `chmod +x setup-one-time && ./setup-one-time`
TODO: Add firewall commands to setup-one-time

Congrats! You're all ready to start the application now! See the next section for more details.

# Starting and Stopping
Start the application by running `docker-compose up` (or `docker-compose up -d` to run in the background). Stop the application by entering ctrl+c or `docker-compose down`.

# Debugging
Start an interactive shell in one of the docker containers that you want to debug:
We have debug scripts to start interactive shells in docker containers:
* For redis: `docker exec -it ergo-index-fund-redis /bin/bash` and then `redis-cli`
* For the main app: `docker exec -it ergo-index-fund-backend /bin/bash`

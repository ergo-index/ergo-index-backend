# Initial Setup (One-Time Only)
1. Install Docker Engine and Docker Compose (and make sure Docker is running)
2. From the project's root directory, run: `chmod +x setup-one-time && ./setup-one-time`
3. Save your Firebase private key as a Docker secret. This assumes you already
have a Firebase account and project created. If you don't, follow the guide from the [frontend project's README](https://github.com/ergo-index/ergo-index-frontend/blob/add_firebase_auth/README.md).
  1. In the Firebase console, open Settings > [Service Accounts](https://console.firebase.google.com/project/_/settings/serviceaccounts/adminsdk)
  2. Click "Generate New Private Key" and save the file
  3. Move the file into the root of this project and rename it to `google_application_credentials.json`

Congrats! You're all ready to start the application now! See the next section for more details.

# Starting and Stopping
Start the application by running `docker-compose up` (or `docker-compose up -d` to run in the background, or `docker-compose up --build` if you made some changes and need to force a rebuild). Stop the application by entering ctrl+c or `docker-compose down`.

# Debugging
Start an interactive shell in one of the docker containers that you want to debug:
We have debug scripts to start interactive shells in docker containers:
* For the main app: `docker exec -it ergo-index-fund-backend /bin/bash`

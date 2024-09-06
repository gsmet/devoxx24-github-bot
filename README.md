# devoxx24-github-bot

> **An AI-infused GitHub bot.**

This bot uses Quarkus, the supersonic subatomic Java framework.

## Setup

Create a `.env` with the following content:

```
QUARKUS_LANGCHAIN4J_OPENAI_API_KEY=<your OpenAI API key>

QUARKUS_GITHUB_APP_APP_ID=<your numeric app id>
QUARKUS_GITHUB_APP_APP_NAME=devoxx24-github-bot
QUARKUS_GITHUB_APP_WEBHOOK_PROXY_URL=<your Smee.io channel URL>
QUARKUS_GITHUB_APP_WEBHOOK_SECRET=<your webhook secret>
QUARKUS_GITHUB_APP_PRIVATE_KEY=-----BEGIN RSA PRIVATE KEY-----\
                  <your private key>                          \
-----END RSA PRIVATE KEY-----
```

You need a PostgreSQL database with a `quarkus` database and a `quarkus` user as the owner listening on the default port (`5432`).

## Ingest the embeddings

You can start the app in dev mode with:

```
mvn clean -Ddevoxx.triage.init=true -Ddevoxx.init.source=issues/ quarkus:dev
```

This will ingest the issues and store the embeddings in the PostgreSQL database.

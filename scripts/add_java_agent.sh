#!/bin/bash

sh ./scripts/find_sponge_mixin.sh | xargs node ./scripts/upsert_java_agent.js

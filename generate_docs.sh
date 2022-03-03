#!/bin/bash

# Copyright 2022 Contacts Contributors
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Fail on any error
set -ex

# Copy over stuff into docs/ to prevent duplication
cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md
cp DEV_NOTES.md docs/dev-notes.md

# Replace links to "/DEV_NOTES" with "/dev-notes"
find docs -type f -name '*.md' -exec sed -i '' 's/\/DEV_NOTES/\/dev-notes/g' {} +

# Remove "/docs" in links
find docs -type f -name '*.md' -exec sed -i '' 's/\/docs//g' {} +

# Replace ".md" with "/" in links
find docs -type f -name '*.md' -exec sed -i '' 's/\.md/\//g' {} +

# View MkDocs
mkdocs serve

# Build MkDocs
# mkdocs build


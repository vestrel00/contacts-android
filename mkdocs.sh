#!/bin/bash

# Fail on any error
set -ex

# Copy over stuff into docs/ to prevent duplication
cp README.md docs/index.md
cp CONTRIBUTING.md docs/contributing.md

# Convert absolute links to relative links in the docs/index.md
sed -i '' 's/\/docs/\./g' docs/index.md

# Replace ".md" with "/" in links
# find docs -type f -name '*.md' -exec sed -i '' 's/\.md/\//g' {} +

# View MkDocs
# mkdocs serve

# Build MkDocs
mkdocs build


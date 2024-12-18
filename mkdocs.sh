#!/bin/bash

# To run this, the following must be installed on the machine;
# MkDocs: https://www.mkdocs.org/user-guide/installation/
# Material for MkDocs: https://squidfunk.github.io/mkdocs-material/getting-started/

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
mkdocs serve

# Build MkDocs
# mkdocs build
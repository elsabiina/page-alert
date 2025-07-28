#!/bin/bash

# Script to copy the latest version of versioned files from a directory
# Usage: ./copy_latest_versions.sh <directory>

# Function to show help
show_help() {
  echo "Usage: $0 <directory>"
  echo ""
  echo "Parameters:"
  echo "  directory    Directory to process"
  echo ""
  echo "Examples:"
  echo "  $0 /path/to/directory"
  echo ""
  echo "The script looks for files with format: filename_X.Y.Z.extension"
  echo "where X.Y.Z is the version (e.g: file_1.2.3.png)"
  echo "and creates a copy with the base name (e.g: file.png)"
}

# Function to compare semantic versions
compare_versions() {
  local v1=$1
  local v2=$2

  # Split versions into arrays
  IFS='.' read -ra V1 <<<"$v1"
  IFS='.' read -ra V2 <<<"$v2"

  # Compare major, minor, patch
  for i in {0..2}; do
    local num1=${V1[i]:-0}
    local num2=${V2[i]:-0}

    if ((num1 > num2)); then
      return 1 # v1 > v2
    elif ((num1 < num2)); then
      return 2 # v1 < v2
    fi
  done

  return 0 # v1 == v2
}

# Function to extract version from filename
extract_version() {
  local filename=$1
  # Look for pattern: filename_X.Y.Z.extension
  if [[ $filename =~ ^(.+)_([0-9]+\.[0-9]+\.[0-9]+)\.(.+)$ ]]; then
    echo "${BASH_REMATCH[2]}"
  else
    echo ""
  fi
}

# Function to extract base name from filename
extract_basename() {
  local filename=$1
  if [[ $filename =~ ^(.+)_([0-9]+\.[0-9]+\.[0-9]+)\.(.+)$ ]]; then
    echo "${BASH_REMATCH[1]}.${BASH_REMATCH[3]}"
  else
    echo ""
  fi
}

# Function to process files and find the most recent version
process_files() {
  local directory=$1

  echo "$(date '+%Y-%m-%d %H:%M:%S') - Processing directory: $directory"

  # Associative arrays to store the latest version of each base file
  declare -A latest_versions
  declare -A latest_files

  # Search for all versioned files
  local files_found=0

  while IFS= read -r -d '' file; do
    filename=$(basename "$file")
    version=$(extract_version "$filename")
    basename_file=$(extract_basename "$filename")

    if [[ -n $version && -n $basename_file ]]; then
      echo "  Found: $filename (version: $version)"
      ((files_found++))

      # If it's the first file of this type or has a higher version
      if [[ -z ${latest_versions[$basename_file]} ]]; then
        latest_versions[$basename_file]=$version
        latest_files[$basename_file]=$file
      else
        compare_versions "$version" "${latest_versions[$basename_file]}"
        result=$?
        if [[ $result -eq 1 ]]; then # New version is higher
          latest_versions[$basename_file]=$version
          latest_files[$basename_file]=$file
        fi
      fi
      # Delete to recreate in the next step
      rm "$basename_file"

    fi
  done < <(find "$directory" -maxdepth 1 -type f -name "*_[0-9]*.[0-9]*.[0-9]*.*" -print0)

  if [[ $files_found -eq 0 ]]; then
    echo "  No versioned files found."
    return 0
  fi

  # Create copies of the most recent versions
  local copies_made=0
  for basename_file in "${!latest_versions[@]}"; do
    source_file="${latest_files[$basename_file]}"
    target_file="$directory/$basename_file"

    echo "  → Creating copy of latest version:"
    echo "    From: $(basename "$source_file") (v${latest_versions[$basename_file]})"
    echo "    To:   $basename_file"

    # Create the copy
    if cp "$source_file" "$target_file"; then
      echo "    ✓ Copy created successfully"
      ((copies_made++))
    else
      echo "    ✗ Error creating copy"
    fi
  done

  echo ""
  echo "Summary: $copies_made copies created from $files_found versioned files."
}

# Parameter validation
if [[ $# -ne 1 || $1 == "-h" || $1 == "--help" ]]; then
  show_help
  exit 0
fi

DIRECTORY=$1

# Validate that directory exists
if [[ ! -d "$DIRECTORY" ]]; then
  echo "Error: Directory '$DIRECTORY' does not exist."
  exit 1
fi

# Process files once
process_files "$DIRECTORY"

#!/usr/bin/env python

COPYRIGHT = '''/*
 * Copyright (C) 2007 Knut Arild Erstad
 *
 * This file is part of Blimp, a layered photo editor.
 *
 * Blimp is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Blimp is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
'''

WRITE_FORMAT = 'wb' # forced unix line endings

import os, sys, re

def read_lines_from_file(file_path):
    file = open(file_path, 'r')
    return file.readlines()

def write_lines_to_file(file_path, lines):
    file = open(file_path, WRITE_FORMAT)
    file.writelines(lines)

def add_copyright_to_java_file(path):
    lines = read_lines_from_file(path)
    index = -1
    for i in range(len(lines)):
        line = lines[i]
        if line.startswith('package '):
            index = i
            break
    if index < 0:
        print 'ERROR: found no package statement in file', path
        return
    write_lines_to_file(path, [COPYRIGHT] + lines[index:])

if __name__ == '__main__':
    for filepath in sys.argv[1:]:
        add_copyright_to_java_file(filepath)

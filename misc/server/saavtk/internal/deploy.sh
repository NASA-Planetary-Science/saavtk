#!/bin/sh
rm -rf output/*
nanoc
scp -r output/* hurlbut:/usr/apache/htdocs-sbmt/internal

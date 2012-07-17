#!/usr/bin/python

import os, glob, subprocess
path = './'
drawables = {'drawable-ldpi':'67,5','drawable':'90','drawable-hdpi':'157,5', 'drawable-xhdpi':'180'}

for density, dpi in drawables.iteritems():
	print (density + dpi)

for infile in glob.glob( os.path.join(path, '*.svg') ):
	print("current file is: " + infile)
	for density, dpi in drawables.iteritems():
		
		original = infile.replace("./","")		
		parsed_name = original.replace(".svg","")

		parameters = infile + " -e " +density +"/"+parsed_name + ".png" + " -d " +dpi		
		command = "inkscape " + parameters
		print (parameters)

		subprocess.call(command, shell=True)


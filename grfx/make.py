#!/usr/bin/python

import os, glob, subprocess
path = './'
drawables = {'ldpi':'67,5','mdpi':'90','hdpi':'157,5', 'xhdpi':'180'}

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

"""inkscape button_next_down.svg -e drawable/button_next_down.png
inkscape button_next_up.svg -e drawable/button_next_up.png
inkscape button_prev_down.svg -e drawable/button_prev_down.png
inkscape button_prev_up.svg -e drawable/button_prev_up.png
inkscape button_stop_down.svg -e drawable/button_stop_down.png
inkscape button_stop_up.svg -e drawable/button_stop_up.png
inkscape button_play_down.svg -e drawable/button_play_down.png
inkscape button_play_up.svg -e drawable/button_play_up.png

inkscape button_next_down.svg -e drawable/button_next_down.png
inkscape button_next_up.svg -e drawable/button_next_up.png
inkscape button_prev_down.svg -e drawable/button_prev_down.png
inkscape button_prev_up.svg -e drawable/button_prev_up.png
inkscape button_stop_down.svg -e drawable/button_stop_down.png
inkscape button_stop_up.svg -e drawable/button_stop_up.png
inkscape button_play_down.svg -e drawable/button_play_down.png
inkscape button_play_up.svg -e drawable/button_play_up.png
"""


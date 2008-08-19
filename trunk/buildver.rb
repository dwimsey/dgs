#
# Copyright (c) 2005
# David Wimsey.  All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
# 1. Redistribution of source code must retain the above copyright
#    notice, this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright
#    notice, this list of conditions and the following disclaimer in the
#    documentation and/or other materials provided with the distribution.
# 3. All advertising materials mentioning features or use of this software
#    must display the following acknowledgement:
#   This product was developed using software by David Wimsey, of SchiZo.com
#   and its contributors.
# 4. All of the remaining copyrights in this document are in full effect.
#    The requirements of the remaining copyrights apply to this file in
#    its entirity.
# 
# THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND HIS/HER CONTRIBUTORS ``AS IS'' AND
# ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
# ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR AND HIS/HER CONTRIBUTORS BE
# LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
# CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
# GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
# HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
# LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
# OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
#   
# CVS Tag: license.txt,v 1.1.1.1 2000/05/16 03:44:36 bits Exp
# -----------------------------------------------------------------------------------------------------
# buildver::buildver.rb
# $Header: /usr/local/var/svn/t/cvs/development/projects/DGS/buildver.rb,v 1.1 2008-08-19 06:15:07 dwimsey Exp $
#

# find the root and the repository information
@cvs_project_branch = "NONE"
@cvs_project_tag = "HEAD"
@cvs_cur_buildcount = 0
@cvs_cur_filecount = 0
@cvs_root = ""
@cvs_repository = ""
@cvs_ignores = Array.new
@cvs_ignores.push("buildver.rb")		# ignore this file and all its related variations
@cvs_ignores.push("librtscrypto/")

Dir.chdir(File::dirname(__FILE__))

if ARGV[0] == nil || ARGV[0].length == 0 || ARGV[0] == ""
	buildinfo_file = ".buildinfo"
else
	buildinfo_file = ARGV[0] + "/.buildinfo"
end
output_type = ""
output_file = ""
begin
	bifh = File.open(buildinfo_file,"r")
	@cvs_project_branch = bifh.gets.chomp
	@major_version = bifh.gets.chomp
	@minor_version = bifh.gets.chomp
	@patchlevel = bifh.gets.chomp
	output_type = bifh.gets.chomp
	output_file = bifh.gets.chomp
	@output_prefix = bifh.gets.chomp
	@patchlevel = 0
	bifh.close
rescue
	st = $@.join("\r\n\t")
	puts "Could not parse .buildinfo: #{$!}\r\n\t#{st}"
	exit(255)
end


@output_types = output_type.downcase.split(/,/)
@output_files = output_file.downcase.split(/,/)
if @output_types.length == @output_files.length
else
	puts "Output types and output file counts are not equal, please correct \'.buildinfo\' and rerun."
	exit(255)
end
@output_types.each {
	|otype|
	case otype
		when 'c'
		when 'nsis'
		when 'nsis_productversion'
		when 'console'
		when 'xml'
			puts "XML output is not yet supported."
			exit(255)
		else
			puts "Unsupported output file type: #{otype}"
			exit(255)
	end
}

cvs_paths = Array.new
cvs_paths.push("/usr/local/bin/cvs")
cvs_paths.push("/usr/bin/cvs")
cvs_paths.push("/bin/cvs")
cvs_paths.push("C:\\Program Files\\GNU\\WinCvs 1.3\\CVSNT\\cvs.exe")
cvs_paths.push("C:\\Program Files\\GNU\\WinCvs 1.3\\CVS\\cvs.exe")
cvs_paths.push("C:\\Program Files\\GNU\\CVSNT\\cvs.exe")
cvs_paths.push("C:\\Program Files\\GNU\\CVS\\cvs.exe")
cvs_paths.push("C:\\Program Files\\CVSNT\\cvs.exe")
cvs_paths.push("C:\\Program Files\\CVS\\cvs.exe")

cvs_binary = ""
cvs_paths.each {
	|tpath|
	if File.exists?(tpath)
		cvs_binary = tpath
		break
	end
}

if cvs_binary == ""
	puts "Could not find cvs binary?"
	exit(255)
end

@tn = Time.now # record the time now to be as accurate as possible
printf "Retrieving version information ..."
cvs_status =`#{cvs_binary} -q status`
if cvs_status.length < 5
	# cvs status returned too little data, probably couldn't find the binary, error out anyway
	puts "cvs status returned < 5 lines, this probably isnt' valid, aborting."
	exit(255)
end

@cvs_cur_file = ""
@cvs_cur_file_rev = ""
@cvs_cur_file_tag = "HEAD"
@cvs_files_str = ""

vc = 0
# Cycle the lines of status and parse out the revision and tag information for each file
cvs_status.each {
	|line|
	line.gsub!(/\r/, "")
	line.gsub!(/\n/, "")
	line.gsub!(/\t/, " ")
	line.gsub!(/ +/, " ")
	if line =~ /^File: (.*) Status:/
		# this line contains our filename
		@cvs_cur_file = "#{$1}"
	end
	if line =~ /Repository revision: ([0-9|\.].+) (.*)$/
		# this line contains our working revision
		file_rev = "#{$1}"
		@cvs_cur_file_rev = "#{$1}"
		@cvs_cur_file_fullpath = "#{$2}"
		vers = file_rev.split(/\./,4)
		vc = 0
		vers.each {
			|rv|
			vc = rv.to_i
		}		
	end
	if line =~ /Sticky Tag: +([a-zA-Z0-9\_\.-]+) ?/
		# this is our tag line, the file doesn't have a tag, use HEAD instead
		ctag = "#{$1}"
		if ctag.downcase == "(none)"
			@cvs_cur_file_tag = "HEAD"
		else
			@cvs_cur_file_tag = ctag
		end
	end
	if line =~ /Sticky Date:/
		if @cvs_cur_file.downcase == ".buildinfo" && @cvs_project_tag == "HEAD"
			@cvs_project_tag = @cvs_cur_file_tag
		else
			# check to see if this file should be ignored
			should_ignore = 0
			@cvs_ignores.each {
				|this_ignore|
				this_match = Regexp.new(this_ignore)
				@cvs_cur_file_fullpath.scan(this_match) {
					should_ignore = 1
				}
			}
			if should_ignore == 0
				@cvs_cur_buildcount += vc
				if @cvs_cur_filecount == 0
					@cvs_files_str += "#{@cvs_cur_file}: #{@cvs_cur_file_rev}"
				else
					@cvs_files_str += "\\r\\n#{@cvs_cur_file}: #{@cvs_cur_file_rev}"
				end
				@cvs_cur_filecount += 1
			end
		end		

		@cvs_cur_file = ""
		@cvs_cur_file_rev = ""
		@cvs_cur_file_branch = ""
		@cvs_cur_file_tag = ""
		vc = 0
	end
}
puts " Done"

@cvs_files_str.gsub!(/\r\n$/, "")
output_file_index = 0
@output_types.each {
	|otype|
	ofile = @output_files[output_file_index]
	output_file_index += 1	# increment now so if we call next to skip part of the loop things will still work right
	output_text = Array.new
	case otype.downcase
		when 'console'
			output_text.push( "     CVS Root: #{@cvs_root.to_s}  Repository: #{@cvs_repository.to_s}")
			output_text.push( "       Branch: #{@cvs_project_branch.to_s}\tTag: #{@cvs_project_tag.to_s}\tVersion: #{@major_version.to_s}.#{@minor_version}\tBuild: #{@cvs_cur_buildcount.to_s}")
			output_text.push( "        Files: #{@cvs_cur_filecount.to_s}")
			output_text.push( "  Output Type: #{otype.to_s}")
			output_text.push( "  Output File: #{ofile.to_s}")
			output_text.push( "Output Prefix: #{@output_prefix.to_s}")
		when 'c'
			output_text.push( "// Autogenerated by buildver.pl (" + "Revision: 1.17".gsub!(/[ $]/, "").gsub!(/Revision:/, "") + ")  Copyright 2005, David Wimsey.  http://www.notresponsible.org/")
			output_text.push("#define #{@output_prefix}VERSION_MAJOR\t#{@major_version}")
			output_text.push("#define #{@output_prefix}VERSION_MINOR\t#{@minor_version}")
			output_text.push("#define #{@output_prefix}VERSION_PATCH\t#{@patchlevel}")
			output_text.push("#define #{@output_prefix}VERSION_BUILD\t#{@cvs_cur_buildcount}")
			output_text.push("#define #{@output_prefix}STRPRIVATEBUILD\t\"#{@cvs_project_branch}\"")
			output_text.push("#define #{@output_prefix}STRSPECIALBUILD\t\"#{@cvs_project_tag}\"")
	#		output_text.push("#define #{@output_prefix}VERSION_STRINGS\t\"#{@cvs_files_str}\"")
			output_text.push("#define #{@output_prefix}BUILD_UTIME\t\"#{Time.now.to_i}\"")
			
			bt = @tn.strftime("%a %b %d %H:%M:%S %Y")
			st = @tn.strftime("%H:%M:%S")
			dt = @tn.strftime("%a %b %d, %Y")
			output_text.push("#define #{@output_prefix}VERSION_BUILDTIME\t\"#{bt}\"")
			output_text.push("#define #{@output_prefix}STRBUILDTIME\t\"Built at #{st} on #{dt}\"")
			output_text.push("#define #{@output_prefix}STRPRODUCTVERSION \"#{@major_version}.#{@minor_version}.#{@patchlevel}.#{@cvs_cur_buildcount}\\0\"")
			output_text.push("#define #{@output_prefix}STRFILEVERSION \"#{@major_version}.#{@minor_version}.#{@patchlevel}.#{@cvs_cur_buildcount}\\0\"")
			output_text.push("#define #{@output_prefix}PRODUCTVERSION #{@major_version}, #{@minor_version}, #{@patchlevel}, #{@cvs_cur_buildcount}")
			output_text.push("#define #{@output_prefix}FILEVERSION #{@major_version}, #{@minor_version}, #{@patchlevel}, #{@cvs_cur_buildcount}")
		when 'nsis'
			output_text.push( "; Autogenerated by buildver.pl (" + "Revision: 1.18".gsub!(/[ $]/, "").gsub!(/Revision:/, "") + ")  Copyright 2005, David Wimsey.  http://www.notresponsible.org/")
			output_text.push("")
			output_text.push("VIProductVersion \"#{@major_version}.#{@minor_version}.#{@patchlevel}.#{@cvs_cur_buildcount}\"")
			output_text.push("VIAddVersionKey /LANG=${LANG_ENGLISH} \"FileVersion\" \"#{@major_version}.#{@minor_version}.#{@patchlevel}.#{@cvs_cur_buildcount}\"")
			output_text.push("VIAddVersionKey /LANG=${LANG_ENGLISH} \"PrivateBuild\" \"#{@cvs_project_branch}\"")
			output_text.push("VIAddVersionKey /LANG=${LANG_ENGLISH} \"SpecialBuild\" \"#{@cvs_project_tag}\"")
			st = @tn.strftime("%H:%M:%S")
			dt = @tn.strftime("%a %b %d, %Y")
			output_text.push("VIAddVersionKey /LANG=${LANG_ENGLISH} \"BuildTime\" \"Built at #{st} on #{dt}\"")
		when 'nsis_productversion'
			output_text.push( "; Autogenerated by buildver.pl (" + "Revision: 1.18".gsub!(/[ $]/, "").gsub!(/Revision:/, "") + ")  Copyright 2005, David Wimsey.  http://www.notresponsible.org/")
			output_text.push("")
			output_text.push("Function InitializeProductVersionTable")
			output_text.push("\tVar /GLOBAL \"ProductProductVersion\"")
			output_text.push("\tVar /GLOBAL \"ProductFileVersion\"")
			output_text.push("\tVar /GLOBAL \"ProductPrivateBuild\"")
			output_text.push("\tVar /GLOBAL \"ProductSpecialBuild\"")
			output_text.push("\tVar /GLOBAL \"ProductBuildTime\"")
			output_text.push("\tVar /GLOBAL \"ProductMajorVersion\"")
			output_text.push("\tVar /GLOBAL \"ProductMinorVersion\"")
			output_text.push("\tVar /GLOBAL \"ProductPatchLevel\"")
			output_text.push("\tVar /GLOBAL \"ProductBuildNumber\"")
			
			

			output_text.push("\tStrCpy $ProductProductVersion \"#{@major_version}.#{@minor_version}.#{@patchlevel}.#{@cvs_cur_buildcount}\"")
			output_text.push("\tStrCpy $ProductFileVersion \"#{@major_version}.#{@minor_version}.#{@patchlevel}.#{@cvs_cur_buildcount}\"")
			output_text.push("\tStrCpy $ProductPrivateBuild \"#{@cvs_project_branch}\"")
			output_text.push("\tStrCpy $ProductSpecialBuild \"#{@cvs_project_tag}\"")
			st = @tn.strftime("%H:%M:%S")
			dt = @tn.strftime("%a %b %d, %Y")
			output_text.push("\tStrCpy $ProductBuildTime \"Built at #{st} on #{dt}\"")
			output_text.push("\tStrCpy $ProductMajorVersion \"#{@major_version}\"")
			output_text.push("\tStrCpy $ProductMinorVersion \"#{@minor_version}\"")
			output_text.push("\tStrCpy $ProductPatchLevel \"#{@patchlevel}\"")
			output_text.push("\tStrCpy $ProductBuildNumber \"#{@cvs_cur_buildcount}\"")
			output_text.push("FunctionEnd")
	end

	output  = File.open("#{ofile}.new", "wb")
	if output == nil
		puts "Could not open output file: #{ofile}.new\r\n\t#{$1}"
		exit(255)
	end

	output_text.each {
		|line|
		output.puts(line)
	}
	output.close

	old_text = Array.new
	new_text = Array.new
	lcount = 0
	begin
		orig_file = File.open(ofile, "rb")
		orig_file.close
		IO.foreach(ofile)  {
			|nline|
			old_text[lcount] = "#{nline}"
			lcount += 1		
		}
		lcount = 0
		begin
			IO.foreach("#{ofile}.new")  {
				|nline|
				new_text[lcount] = "#{nline}"
				lcount += 1
			}
		rescue
			puts "Error reading new version information. Aborting: #{$!}"
			exit(255)
		end



		if old_text.length != new_text.length
			puts "Updating #{ofile}.  Build: #{@cvs_cur_buildcount}"
			File.rename("#{ofile}.new", ofile)
		else
			file_renamed = 0
			ccount = 0
			lcount = new_text.length
			while ccount < lcount
				oline = old_text[ccount]
				nline = new_text[ccount]
				
				if oline != nline
					if oline =~ /TIME/ && nline =~ /TIME/
					else
						if oline =~ /BuildTime/ && nline =~ /BuildTime/
						else
							puts "Changed line: #{oline}"
							puts "Updating #{ofile}.  Build: #{@cvs_cur_buildcount}"
							File.rename("#{ofile}.new", ofile)
							ccount = lcount
							file_renamed = 1
						end
					end
				end
				ccount += 1 
			end

			if file_renamed == 0
				File.unlink("#{ofile}.new")
			end
		end
	rescue
		puts "No existing version information, updating #{ofile}.  Build: #{@cvs_cur_buildcount}"
		begin
			File.rename("#{ofile}.new", ofile)
		rescue
			puts "Could not rename file: #{ofile}.new -> #{ofile}:\r\n\t#{$!}"
			exit(255)
		end
	end
}
puts "Version information is up to date.  Build: #{@cvs_cur_buildcount}"

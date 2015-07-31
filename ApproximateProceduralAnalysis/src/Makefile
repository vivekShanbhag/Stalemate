all: deadlock-detection.w
	nuweb -n deadlock-detection.w
	chmod a+x grepNDcut grepNDcutInLoop
	latex deadlock-detection.tex 
	latex deadlock-detection.tex 
	javac LockNdThread.java
	javac SynchNdThread.java
	javac JavaBlob.java CodeBlob.java
	/usr/java/j2sdk1.4.2_12/bin/javac JavaBlob.java CodeBlob.java
	javac Test.java Foo.java RunTests.java Dl.java
	# /usr/java/jdk1.5.0_08/bin/java -Xmx256m JavaBlob /usr/java/jdk1.5.0_08/jre/lib/rt.jar > 1.5.0.Lst &
	# /usr/java/jdk1.6.0/bin/java -Xmx256m JavaBlob /usr/java/jdk1.6.0/jre/lib/rt.jar > 1.6.0.Lst &

techvista: deadlock-detection.w
	# make all
	time ../j2sdk1.4.2_13/bin/java -Xmx314m SynchNdThread > 1.4.2_13
	time ../j2sdk1.4.2_14/bin/java -Xmx314m SynchNdThread > 1.4.2_14

kkzb:
	time ../jdk1.5.0/bin/java -Xmx414m JavaBlob > 1.5.0_00.Lst
	time ../jdk1.5.0_01/bin/java -Xmx414m JavaBlob > 1.5.0_01.Lst
	time ../jdk1.5.0_02/bin/java -Xmx414m JavaBlob > 1.5.0_02.Lst
	time ../jdk1.5.0_03/bin/java -Xmx414m JavaBlob > 1.5.0_03.Lst
	time ../jdk1.5.0_04/bin/java -Xmx414m JavaBlob > 1.5.0_04.Lst
	time ../jdk1.5.0_05/bin/java -Xmx414m JavaBlob > 1.5.0_05.Lst
	time ../jdk1.5.0_06/bin/java -Xmx414m JavaBlob > 1.5.0_06.Lst
	time ../jdk1.5.0_07/bin/java -Xmx414m JavaBlob > 1.5.0_07.Lst
	time /usr/java/jdk1.5.0_08/bin/java -Xmx414m JavaBlob > 1.5.0_08.Lst
	time ../jdk1.5.0_09/bin/java -Xmx414m JavaBlob > 1.5.0_09.Lst
	time ../jdk1.5.0_10/bin/java -Xmx414m JavaBlob > 1.5.0_10.Lst
	time ../jdk1.5.0_11/bin/java -Xmx414m JavaBlob > 1.5.0_11.Lst
	time ../jdk1.5.0_12/bin/java -Xmx414m JavaBlob > 1.5.0_12.Lst
	time ../j2sdk1.4.2/bin/java -Xmx414m JavaBlob > 1.4.2_00.Lst
	time ../j2sdk1.4.2_01/bin/java -Xmx414m JavaBlob > 1.4.2_01.Lst
	time ../j2sdk1.4.2_02/bin/java -Xmx414m JavaBlob > 1.4.2_02.Lst
	time ../j2sdk1.4.2_03/bin/java -Xmx414m JavaBlob > 1.4.2_03.Lst
	time /usr/java/j2sdk1.4.2_04/bin/java -Xmx414m JavaBlob > 1.4.2_04.Lst
	time ../j2sdk1.4.2_05/bin/java -Xmx414m JavaBlob > 1.4.2_05.Lst
	time ../j2sdk1.4.2_06/bin/java -Xmx414m JavaBlob > 1.4.2_06.Lst
	time ../j2sdk1.4.2_07/bin/java -Xmx414m JavaBlob > 1.4.2_07.Lst
	time ../j2sdk1.4.2_08/bin/java -Xmx414m JavaBlob > 1.4.2_08.Lst
	time ../j2sdk1.4.2_09/bin/java -Xmx414m JavaBlob > 1.4.2_09.Lst
	time ../j2sdk1.4.2_10/bin/java -Xmx414m JavaBlob > 1.4.2_10.Lst
	time ../j2sdk1.4.2_11/bin/java -Xmx414m JavaBlob > 1.4.2_11.Lst
	time /usr/java/j2sdk1.4.2_12/bin/java -Xmx414m JavaBlob > 1.4.2_12.Lst
	time ../j2sdk1.4.2_13/bin/java -Xmx414m JavaBlob > 1.4.2_13.Lst
	time ../j2sdk1.4.2_14/bin/java -Xmx414m JavaBlob > 1.4.2_14.Lst
	time ../jdk1.5.0/bin/java -Xmx314m SynchNdThread > 1.5.0_00
	time ../jdk1.5.0_01/bin/java -Xmx314m SynchNdThread > 1.5.0_01
	time ../jdk1.5.0_02/bin/java -Xmx314m SynchNdThread > 1.5.0_02
	time ../jdk1.5.0_03/bin/java -Xmx314m SynchNdThread > 1.5.0_03
	time ../jdk1.5.0_04/bin/java -Xmx314m SynchNdThread > 1.5.0_04
	time ../jdk1.5.0_05/bin/java -Xmx314m SynchNdThread > 1.5.0_05
	time ../jdk1.5.0_06/bin/java -Xmx314m SynchNdThread > 1.5.0_06
	time ../jdk1.5.0_07/bin/java -Xmx314m SynchNdThread > 1.5.0_07
	time /usr/java/jdk1.5.0_08/bin/java -Xmx314m SynchNdThread > 1.5.0_08
	time ../jdk1.5.0_09/bin/java -Xmx314m SynchNdThread > 1.5.0_09
	time ../jdk1.5.0_10/bin/java -Xmx314m SynchNdThread > 1.5.0_10
	time ../jdk1.5.0_11/bin/java -Xmx314m SynchNdThread > 1.5.0_11
	time ../jdk1.5.0_12/bin/java -Xmx314m SynchNdThread > 1.5.0_12
	time ../j2sdk1.4.2/bin/java -Xmx314m SynchNdThread > 1.4.2_00
	time ../j2sdk1.4.2_01/bin/java -Xmx314m SynchNdThread > 1.4.2_01
	time ../j2sdk1.4.2_02/bin/java -Xmx314m SynchNdThread > 1.4.2_02
	time ../j2sdk1.4.2_03/bin/java -Xmx314m SynchNdThread > 1.4.2_03
	time /usr/java/j2sdk1.4.2_04/bin/java -Xmx314m SynchNdThread > 1.4.2_04
	time ../j2sdk1.4.2_05/bin/java -Xmx314m SynchNdThread > 1.4.2_05
	time ../j2sdk1.4.2_06/bin/java -Xmx314m SynchNdThread > 1.4.2_06
	time ../j2sdk1.4.2_07/bin/java -Xmx314m SynchNdThread > 1.4.2_07
	time ../j2sdk1.4.2_08/bin/java -Xmx314m SynchNdThread > 1.4.2_08
	time ../j2sdk1.4.2_09/bin/java -Xmx314m SynchNdThread > 1.4.2_09
	time ../j2sdk1.4.2_10/bin/java -Xmx314m SynchNdThread > 1.4.2_10
	time ../j2sdk1.4.2_11/bin/java -Xmx314m SynchNdThread > 1.4.2_11
	time /usr/java/j2sdk1.4.2_12/bin/java -Xmx314m SynchNdThread > 1.4.2_12
	time ../j2sdk1.4.2_13/bin/java -Xmx314m SynchNdThread > 1.4.2_13
	time ../j2sdk1.4.2_14/bin/java -Xmx314m SynchNdThread > 1.4.2_13

all:
	~/nuweb/nuweb last.w
	export TEXINPUTS=".:./titlesec/:"; latex last.tex
	export TEXINPUTS=".:./titlesec/:"; latex last.tex
	mkdir deadlockPrediction; mv CodeBlob.java OTlock.java ClassInfo.java deadlockPrediction
	mv TypeSignature.java MethodSignature.java deadlockPrediction
	rm -f ListMap.java ListMapMXBean.java Main.java Mapper.java QueueSample.java
	#/usr/local/share/jdk1.6.0_35/bin/javac -g deadlock/*.java
	/usr/local/share/jdk1.6.0_35/bin/javac -g deadlockPrediction/CodeBlob.java deadlockPrediction/OTlock.java deadlockPrediction/ClassInfo.java deadlockPrediction/TypeSignature.java deadlockPrediction/MethodSignature.java JavaBlob.java

SUBMIT_PATH="submit"
GROUP_PATH="Group2"

set -x

rm -rf $SUBMIT_PATH/*
mkdir -p $SUBMIT_PATH/$GROUP_PATH

# jar files
cd fullProjectAssembly/
mvn clean install
cd ..
cp fullProjectAssembly/target/* $SUBMIT_PATH/$GROUP_PATH/

# lib
cp fullProjectAssembly/lib/* $SUBMIT_PATH/$GROUP_PATH/

# readme
cp README.txt $SUBMIT_PATH/$GROUP_PATH/

# all the source code
cd fullProjectAssembly/
mvn clean
cd ..
cp -r fullProjectAssembly/ $SUBMIT_PATH/$GROUP_PATH/

# zip it
cd $SUBMIT_PATH
zip -r ../$GROUP_PATH.zip *

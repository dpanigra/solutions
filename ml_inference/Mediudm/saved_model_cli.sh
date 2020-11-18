######## 1. install saved_model_cli in the Mac/Dataproc/Google Cloud Engine node ##############
# install in mac (recommended - you will use for you local development)
sudo pip3 install tensorflow
# find the path to the saved_model_cli
find ./ -name saved_model_cli 2>/dev/null
export PATH_TO_SAVED_MODEL_CLI=<your path>
# export PATH_TO_SAVED_MODEL_CLI=/Library/Frameworks/Python.framework/Versions/3.6/
# export PATH_TO_SAVED_MODEL_CLI=/usr/local/bin/
echo $PATH_TO_SAVED_MODEL_CLI
ls -ltrah ${PATH_TO_SAVED_MODEL_CLI}/saved_model_cli
# check the version of the tool
$PATH_TO_SAVED_MODEL_CLI/saved_model_cli --version
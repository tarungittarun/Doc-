#!/usr/bin/env bash

##############################################################################
##
##  Gradle start up script for UN*X
##
##############################################################################

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (must be 'true' or 'false').
cygwin=false
msys=false
darwin=false
nonstop=false
case "$(uname)" in
  CYGWIN* ) cygwin=true ;;
  MINGW* ) msys=true ;;
  Darwin* ) darwin=true ;;
  CYGWIN* ) nonstop=true ;;
esac

# Attempt to set APP_HOME
APP_HOME=$( cd "${BASH_SOURCE[0]%/*}/" && pwd ) || exit

# Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
DEFAULT_JVM_OPTS='"-Xmx64m" "-Xms64m"'

# For Cygwin, ensure paths are in UNIX format before anything is touched.
if $cygwin ; then
    APP_HOME=$(cygpath --path --mixed "$APP_HOME")
    CLASSPATH=$(cygpath --path --mixed "$CLASSPATH")
    JAVACMD=$(cygpath --path --mixed "$JAVACMD")
fi

# For darwin, add the default Java home if not set.
if $darwin ; then
    if [ -z "$JAVA_HOME" ] ; then
        /usr/libexec/java_home || true
    fi
fi

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if [ ! -x "$JAVACMD" ] ; then
    die "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.

Please set the JAVA_HOME variable in your environment to match the
location of your Java installation."
fi

# Split GRADLE_OPTS if set, and add them to the list.
if [ -n "$GRADLE_OPTS" ]; then
    IFS=' ' read -r -a gradle_opts_array <<< "$GRADLE_OPTS"
else
    gradle_opts_array=()
fi

# Collect all arguments for the java command:
#   * DEFAULT_JVM_OPTS
#   * JAVA_OPTS
#   * GRADLE_OPTS
#   * -Dorg.gradle.appname=$APP_BASE_NAME
#   * -classpath
#   * $APP_HOME/gradle/wrapper/gradle-wrapper.jar
#   * org.gradle.wrapper.GradleWrapperMain
#   * environment variables
#   * command line arguments
java_cmd=(
    "$JAVACMD"
    "${DEFAULT_JVM_OPTS[@]}"
    "${JAVA_OPTS[@]}"
    "${gradle_opts_array[@]}"
    "-Dorg.gradle.appname=$APP_BASE_NAME"
    -classpath
    "$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
    org.gradle.wrapper.GradleWrapperMain
    "$@"
)

exec "${java_cmd[@]}"

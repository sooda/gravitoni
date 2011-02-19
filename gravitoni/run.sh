CLASSPATH=bin java gravitoni.Gravitoni conf/earthsun.conf $@|tail -n +8 > fulllog
grep Earth fulllog > earth.log
grep Sun fulllog > sun.log

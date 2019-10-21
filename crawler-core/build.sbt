mappings in(Compile, packageBin) ~= {
  files =>
    files.filter(!_._1.getName.contentEquals("log4j2.xml"))
}

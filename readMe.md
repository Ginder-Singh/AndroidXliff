# Android Xlf converter
Simple utility which converts android localization xml files to xlf and vice versa.
## Build & Run
1. Run "./gradlew shadowJar"
2. .Xlf to Xml: $ java -jar build/libs/xlf.jar toXml ./sample_res/xlf ./sample_res/xml
3. Xml to .Xlf: $ java -jar build/libs/xlf.jar toXliff ./sample_res ar,hi
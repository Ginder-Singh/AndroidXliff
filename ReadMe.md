# Android Xlf converter
Simple utility which converts android localization xml files to xlf and vice versa.
## Build & Run
1. Run ".gradlew shadowJar"
2. .Xlf to Xml: "java -jar build/libs/xlf.jar toXml /path/to/xlf /path/to/xml"
3. Xml to .Xlf:  "java -jar build/libs/xlf.jar toXliff /path/to/android/resource/folder /comma/separated/list/of/language/codes"
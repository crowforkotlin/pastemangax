# 指定 CMake 最低版本要求
cmake_minimum_required(VERSION 3.4.1)

# 设置项目名
project(ChineseConverter)

# 设置 C++ 标准版本
set(CMAKE_CXX_STANDARD 11)

# 设置 Android 平台版本
set(CMAKE_SYSTEM_VERSION 23)

# 设置 STL 类型
set(CMAKE_ANDROID_STL_TYPE c++_static)

# 添加编译选项
add_compile_options(-fexceptions)

# 添加源文件 编译成名为OpenCC的静态库（编译时链接到程序中）
add_library(OpenCC STATIC
        src/main/jni/OpenCC/src/BinaryDict.cpp
        src/main/jni/OpenCC/src/Config.cpp
        src/main/jni/OpenCC/src/Conversion.cpp
        src/main/jni/OpenCC/src/ConversionChain.cpp
        src/main/jni/OpenCC/src/Converter.cpp
        src/main/jni/OpenCC/src/DartsDict.cpp
        src/main/jni/OpenCC/src/Dict.cpp
        src/main/jni/OpenCC/src/DictEntry.cpp
        src/main/jni/OpenCC/src/DictGroup.cpp
        src/main/jni/OpenCC/src/Lexicon.cpp
        src/main/jni/OpenCC/src/MarisaDict.cpp
        src/main/jni/OpenCC/src/MaxMatchSegmentation.cpp
        src/main/jni/OpenCC/src/PhraseExtract.cpp
        src/main/jni/OpenCC/src/SerializedValues.cpp
        src/main/jni/OpenCC/src/Segmentation.cpp
        src/main/jni/OpenCC/src/SimpleConverter.cpp
        src/main/jni/OpenCC/src/TextDict.cpp
        src/main/jni/OpenCC/src/UTF8StringSlice.cpp
        src/main/jni/OpenCC/src/UTF8Util.cpp
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/trie.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/agent.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/grimoire/io/reader.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/grimoire/io/writer.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/grimoire/io/mapper.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/grimoire/trie/louds-trie.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/grimoire/trie/tail.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/grimoire/vector/bit-vector.cc
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/marisa/keyset.cc
)

# 添加编译标志
target_compile_definitions(OpenCC PRIVATE OPENCC_ENABLE_DARTS)

# 设置包含目录
target_include_directories(OpenCC PUBLIC
        src/main/jni/OpenCC/src/
        src/main/jni/OpenCC/deps/darts-clone/
        src/main/jni/OpenCC/deps/marisa-0.2.6/include/
        src/main/jni/OpenCC/deps/marisa-0.2.6/lib/
        src/main/jni/OpenCC/deps/rapidjson-1.1.0/
        src/main/jni/OpenCC/deps/rapidjson-1.1.0/rapidjson
)

# 添加源文件 编译成名为ChineseConverter的共享库（共享库是动态链接的，运行时被加载）
add_library(ChineseConverter SHARED src/main/cpp/ChineseConverter.cpp)

# 链接静态库和动态库
target_link_libraries(ChineseConverter OpenCC log android)
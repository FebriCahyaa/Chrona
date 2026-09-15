cmake_minimum_required(VERSION 3.22.1)
project(chrona_clock LANGUAGES CXX)

add_library(chrona_clock SHARED chrona_clock.cpp)

target_compile_features(chrona_clock PRIVATE cxx_std_20)
target_compile_options(chrona_clock PRIVATE
    -O2
    -ffast-math
    -fvisibility=hidden
    -fno-exceptions
    -fno-rtti
)

target_link_libraries(chrona_clock android log)

# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.

cmake_minimum_required(VERSION 3.22.1)
project(chrona_clock LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(chrona_clock SHARED
    chrona_clock.cpp
    chrona_time.cpp
)

target_compile_features(chrona_clock PRIVATE cxx_std_20)
target_compile_options(chrona_clock PRIVATE
    -O2
    -fvisibility=hidden
    -fno-exceptions
    -fno-rtti
    -Wall
    -Wextra
    -Werror=return-type
)

target_link_libraries(chrona_clock android log)

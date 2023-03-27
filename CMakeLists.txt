cmake_minimum_required(VERSION 3.0)

# Create the project and tell cmake that this is a C only project...
#   by default cmake will want to have both a C and C++ compiler but
#   a C++ compiler isn't currently installed on b8cp275
project(chat_sys, C)

# Tell cmake which source files are needed for each of the binaries to build
add_executable(chat_client client.c)
add_executable(chat_server server.c)

# point.c will probably need sqrt, which means we need to tell cmake
# to include the math library (-lpthread) when creating the binary
target_link_libraries(chat_client pthread)
target_link_libraries(chat_server pthread)


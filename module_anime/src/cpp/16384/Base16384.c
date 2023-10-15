/* base16384.c
 * This file is part of the base16384 distribution (https://github.com/fumiama/base16384).
 * Copyright (c) 2022-2023 Fumiama Minamoto.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

#ifndef __cosmopolitan
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <time.h>
#ifdef _WIN32
#include <windows.h>
#endif
#endif
#include "base16384.h"

char encbuf[BASE16384_ENCBUFSZ];
char decbuf[BASE16384_DECBUFSZ];

#ifndef _WIN32
unsigned long get_start_ms() {
    struct timespec ts;
    clock_gettime(CLOCK_MONOTONIC, &ts);
    return (ts.tv_sec * 1000 + ts.tv_nsec / 1000000);
}
#endif

static void print_usage() {
    puts("Copyright (c) 2022-2023 Fumiama Minamoto.\nBase16384 2.2.5 (August 26th 2023). Usage:");
    puts("base16384 [-edt] [inputfile] [outputfile]");
    puts("  -e\t\tencode");
    puts("  -d\t\tdecode");
    puts("  -t\t\tshow spend time");
    puts("  inputfile\tpass - to read from stdin");
    puts("  outputfile\tpass - to write to stdout");
}

int main(int argc, char** argv) {
    if(argc != 4 || argv[1][0] != '-') {
        print_usage();
        return -1;
    }
    int flaglen = strlen(argv[1]);
    if(flaglen <= 1 || flaglen > 3) {
        print_usage();
        return -2;
    }
#ifdef _WIN32
    clock_t t = 0;
#else
    unsigned long t = 0;
#endif
    base16384_err_t exitstat = base16384_err_ok;
    char cmd = argv[1][1];
    if(cmd == 't') {
        if(flaglen == 2) {
            print_usage(); return -3;
        }
#ifdef _WIN32
        t = clock();
#else
        t = get_start_ms();
#endif
        cmd = argv[1][2];
    } else if(flaglen == 3) {
        if(argv[1][2] != 't') {
            print_usage(); return -4;
        }
#ifdef _WIN32
        t = clock();
#else
        t = get_start_ms();
#endif
    }
    switch(cmd) {
        case 'e': exitstat = base16384_encode_file(argv[2], argv[3], encbuf, decbuf); break;
        case 'd': exitstat = base16384_decode_file(argv[2], argv[3], encbuf, decbuf); break;
        default: print_usage(); return -5;
    }
    if(t && !exitstat && *(uint16_t*)(argv[3]) != *(uint16_t*)"-") {
#ifdef _WIN32
        printf("spend time: %lums\n", clock() - t);
#else
        printf("spend time: %lums\n", get_start_ms() - t);
#endif
    }
#define print_base16384_err(n) case base16384_err_##n: perror("base16384_err_"#n); break
    if(exitstat) switch(exitstat) {
            print_base16384_err(get_file_size);
            print_base16384_err(fopen_output_file);
            print_base16384_err(fopen_input_file);
            print_base16384_err(write_file);
            print_base16384_err(open_input_file);
            print_base16384_err(map_input_file);
            print_base16384_err(read_file);
            print_base16384_err(invalid_file_name);
            default: perror("base16384"); break;
        }
#undef print_base16384_err
    return exitstat;
}
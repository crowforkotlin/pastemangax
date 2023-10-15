#ifndef _BASE16384_H_
#define _BASE16384_H_

/* base16384.h
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
#include <stdint.h>
#include <stdio.h>
#endif

#define define_base16384_err_t(n) base16384_err_##n

// base16384_err_t is the return value of base16384_en/decode_file
enum base16384_err_t {
    define_base16384_err_t(ok),
    define_base16384_err_t(get_file_size),
    define_base16384_err_t(fopen_output_file),
    define_base16384_err_t(fopen_input_file),
    define_base16384_err_t(write_file),
    define_base16384_err_t(open_input_file),
    define_base16384_err_t(map_input_file),
    define_base16384_err_t(read_file),
    define_base16384_err_t(invalid_file_name),
};
// base16384_err_t is the return value of base16384_en/decode_file
typedef enum base16384_err_t base16384_err_t;

#undef define_base16384_err_t

#define BASE16384_ENCBUFSZ (BUFSIZ*1024/7*7+7)
#define BASE16384_DECBUFSZ (BUFSIZ*1024/8*8+16)

// base16384_encode_len calc min buf size to fill encode result
static inline int base16384_encode_len(int dlen) {
    int outlen = dlen / 7 * 8;
    int offset = dlen % 7;
    switch(offset) {	// 算上偏移标志字符占用的2字节
        case 0: break;
        case 1: outlen += 4; break;
        case 2:
        case 3: outlen += 6; break;
        case 4:
        case 5: outlen += 8; break;
        case 6: outlen += 10; break;
        default: break;
    }
    return outlen + 8;	// 冗余的8B用于可能的结尾的覆盖
}

// base16384_decode_len calc min buf size to fill decode result
static inline int base16384_decode_len(int dlen, int offset) {
    int outlen = dlen;
    switch(offset) {	// 算上偏移标志字符占用的2字节
        case 0: break;
        case 1: outlen -= 4; break;
        case 2:
        case 3: outlen -= 6; break;
        case 4:
        case 5: outlen -= 8; break;
        case 6: outlen -= 10; break;
        default: break;
    }
    return outlen / 8 * 7 + offset + 1; // 多出1字节用于循环覆盖
}

// base16384_encode encodes data and write result into buf
int base16384_encode(const char* data, int dlen, char* buf);

// base16384_decode decodes data and write result into buf
int base16384_decode(const char* data, int dlen, char* buf);

// base16384_encode_file encodes input file to output file.
//    use `-` to specify stdin/stdout
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
base16384_err_t base16384_encode_file(const char* input, const char* output, char* encbuf, char* decbuf);

// base16384_encode_fp encodes input file to output file.
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
base16384_err_t base16384_encode_fp(FILE* input, FILE* output, char* encbuf, char* decbuf);

// base16384_encode_fd encodes input fd to output fd.
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
base16384_err_t base16384_encode_fd(int input, int output, char* encbuf, char* decbuf);

// base16384_decode_file decodes input file to output file.
//    use `-` to specify stdin/stdout
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
base16384_err_t base16384_decode_file(const char* input, const char* output, char* encbuf, char* decbuf);

// base16384_decode_fp decodes input file to output file.
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
base16384_err_t base16384_decode_fp(FILE* input, FILE* output, char* encbuf, char* decbuf);

// base16384_decode_fd decodes input fd to output fd.
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
base16384_err_t base16384_decode_fd(int input, int output, char* encbuf, char* decbuf);

#endif
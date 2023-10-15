//
// Created by fumiama on 2021/5/20.
//

#ifndef BASE16384_BASE16384_HPP
#define BASE16384_BASE16384_HPP
#include <stdint.h>
#include "base16384.h"

// base16384_encode encodes data and write result into buf
extern "C" int base16384_encode(const char* data, int dlen, char* buf, int blen);

// base16384_decode decodes data and write result into buf
extern "C" int base16384_decode(const char* data, int dlen, char* buf, int blen);

// base16384_encode_file encodes input file to output file.
//    use `-` to specify stdin/stdout
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
extern "C" base16384_err_t base16384_encode_file(const char* input, const char* output, char* encbuf, char* decbuf);

// base16384_decode_file decodes input file to output file.
//    use `-` to specify stdin/stdout
//    encbuf & decbuf must be no less than BASE16384_ENCBUFSZ & BASE16384_DECBUFSZ
extern "C" base16384_err_t base16384_decode_file(const char* input, const char* output, char* encbuf, char* decbuf);

#endif //BASE16384_BASE16384_HPP
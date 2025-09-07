import argparse
import sys
from pathlib import Path
import qrcode
from qrcode.constants import ERROR_CORRECT_L, ERROR_CORRECT_M, ERROR_CORRECT_Q, ERROR_CORRECT_H

ERR = {"L": ERROR_CORRECT_L, "M": ERROR_CORRECT_M, "Q": ERROR_CORRECT_Q, "H": ERROR_CORRECT_H}

def main():
    p = argparse.ArgumentParser(description="Generate a PNG QR code.")
    p.add_argument("data", help="Text/URL to encode, or '-' to read from stdin.")
    p.add_argument("-o", "--out", default="qr.png", help="Output PNG path")
    p.add_argument("--err", choices=list(ERR), default="M", help="Error correction (L/M/Q/H)")
    p.add_argument("--box-size", type=int, default=10, help="Pixel size per module")
    p.add_argument("--border", type=int, default=4, help="Border width (modules)")
    p.add_argument("--fill", default="black", help="Fill color (name or hex)")
    p.add_argument("--back", default="white", help="Background color (name or hex)")
    args = p.parse_args()

    payload = sys.stdin.read() if args.data == "-" else args.data
    if not payload.strip():
        print("No data to encode.", file=sys.stderr)
        sys.exit(2)

    qr = qrcode.QRCode(
        version=None,
        error_correction=ERR[args.err],
        box_size=args.box_size,
        border=args.border,
    )
    qr.add_data(payload)
    qr.make(fit=True)
    img = qr.make_image(fill_color=args.fill, back_color=args.back)

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out)
    print(f"Wrote {out.resolve()}")

if __name__ == "__main__":
    main()
import argparse
import sys
from pathlib import Path
import qrcode
from qrcode.constants import ERROR_CORRECT_L, ERROR_CORRECT_M, ERROR_CORRECT_Q, ERROR_CORRECT_H

# Constants that the library uses to control how much of the QR image can be damaged and still be readable.
# L ≈ 7% error recovery
# M ≈ 15% (common default)
# Q ≈ 25%
# H ≈ 30% (most robust; makes codes denser/larger)
ERR = {"L": ERROR_CORRECT_L, "M": ERROR_CORRECT_M, "Q": ERROR_CORRECT_Q, "H": ERROR_CORRECT_H}

def main():
    p = argparse.ArgumentParser(description="Generate a PNG QR code.")
    p.add_argument("data", help="Text/URL to encode")
    p.add_argument("-o", "--out", default="qr.png", help="Output PNG path")
    p.add_argument("--err", choices=list(ERR), default="M", help="Error correction (L/M/Q/H)")
    p.add_argument("--box-size", type=int, default=10, help="Pixel size per module")
    args = p.parse_args()

    payload = args.data
    if not payload.strip() or payload.strip() == '-':
        print("No data to encode.", file=sys.stderr)
        sys.exit(2)

    qr = qrcode.QRCode(
        version=None,
        error_correction=ERR[args.err],
        box_size=args.box_size
    )
    qr.add_data(payload)
    qr.make(fit=True)
    img = qr.make_image()

    out = Path(args.out)
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out)
    print(f"Wrote {out.resolve()}")

if __name__ == "__main__":
    main()

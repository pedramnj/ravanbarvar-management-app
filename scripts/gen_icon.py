from PIL import Image, ImageDraw
import math, os

OUT = "/Users/pedro/ravanbarvar-patient-manager/app/src/main/res"

TEAL = (76, 138, 130)      # #4C8A82
SAGE = (139, 176, 139)     # #8BB08B
LAV  = (142, 124, 195)     # #8E7CC3
WHITE = (255, 255, 255)

def lerp(a, b, t):
    return tuple(int(a[i] + (b[i]-a[i])*t) for i in range(3))

def gradient_square(size, c1, c2):
    img = Image.new("RGB", (size, size), c1)
    px = img.load()
    for y in range(size):
        for x in range(size):
            t = (x + y) / (2*size)
            px[x, y] = lerp(c1, c2, t)
    return img

def rounded_mask(size, radius_ratio):
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    r = int(size*radius_ratio)
    d.rounded_rectangle([0, 0, size-1, size-1], radius=r, fill=255)
    return mask

def circle_mask(size):
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse([0, 0, size-1, size-1], fill=255)
    return mask

def draw_mark(draw, cx, cy, r, color, width):
    # Abstract mark: a calm "mind + leaf/growth" glyph.
    # Outer open circle (wholeness / mind), with a leaf/sprout rising from the base.
    bbox = [cx-r, cy-r, cx+r, cy+r]
    draw.arc(bbox, start=35, end=305, fill=color, width=width)
    # Leaf: two arcs meeting at top and bottom forming a vesica (growth leaf) inside the circle
    leaf_h = r*1.25
    leaf_w = r*0.62
    top = (cx, cy - leaf_h*0.55)
    bot = (cx, cy + leaf_h*0.55)
    # left arc of leaf
    box_l = [cx-leaf_w, cy-leaf_h*0.55, cx+leaf_w*0.15, cy+leaf_h*0.55]
    box_r = [cx-leaf_w*0.15, cy-leaf_h*0.55, cx+leaf_w, cy+leaf_h*0.55]
    draw.arc(box_l, start=300, end=60, fill=color, width=width)
    draw.arc(box_r, start=120, end=240, fill=color, width=width)
    # small stem dot at base
    stem_r = width*0.9
    draw.ellipse([cx-stem_r, bot[1]-stem_r+width*0.6, cx+stem_r, bot[1]+stem_r+width*0.6], fill=color)

def make_foreground(size):
    img = Image.new("RGBA", (size, size), (0,0,0,0))
    draw = ImageDraw.Draw(img)
    cx, cy = size//2, size//2
    r = int(size*0.20)
    width = max(6, int(size*0.045))
    draw_mark(draw, cx, cy, r, WHITE + (255,), width)
    return img

def make_background(size):
    return gradient_square(size, TEAL, LAV).convert("RGBA")

def flatten_legacy(size, mask_type="round_rect"):
    bg = make_background(size)
    fg_full = make_foreground(int(size*2.6))
    fg = fg_full.resize((int(size*0.72), int(size*0.72)), Image.LANCZOS)
    canvas = bg.copy()
    off = ((size - fg.width)//2, (size - fg.height)//2)
    canvas.paste(fg, off, fg)
    if mask_type == "round_rect":
        mask = rounded_mask(size, 0.19)
    else:
        mask = circle_mask(size)
    out = Image.new("RGBA", (size, size), (0,0,0,0))
    out.paste(canvas, (0,0), mask)
    return out

# Adaptive icon layers (432px, full-bleed 108dp @4x)
adaptive_size = 432
bg_img = make_background(adaptive_size)
fg_img = make_foreground(adaptive_size)

for density_dir in ["mipmap-xxxhdpi"]:
    d = os.path.join(OUT, density_dir)
    os.makedirs(d, exist_ok=True)
    bg_img.save(os.path.join(d, "ic_launcher_background.png"))
    fg_img.save(os.path.join(d, "ic_launcher_foreground.png"))

# Legacy launcher icons per density (square rounded + round variant)
densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}
for dname, size in densities.items():
    d = os.path.join(OUT, dname)
    os.makedirs(d, exist_ok=True)
    sq = flatten_legacy(size, "round_rect")
    rd = flatten_legacy(size, "circle")
    sq.save(os.path.join(d, "ic_launcher.png"))
    rd.save(os.path.join(d, "ic_launcher_round.png"))

# Feature graphic style splash mark (used inside app, not launcher) - export a clean 512 mark on transparent bg
splash = Image.new("RGBA", (512,512), (0,0,0,0))
d = ImageDraw.Draw(splash)
draw_mark(d, 256, 256, 105, TEAL+(255,), 24)
splash.save(os.path.join(OUT, "drawable", "ic_brand_mark.png"))

print("done")

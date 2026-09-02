from PIL import Image, ImageDraw
import os

RES = "/Users/pedro/ravanbarvar-patient-manager/app/src/main/res"
MARK_PATH = os.path.join(RES, "drawable", "logo_mark.png")

CREAM = (250, 247, 242, 255)  # matches WarmBackgroundLight

mark_master = Image.open(MARK_PATH).convert("RGBA")

def rounded_mask(size, radius_ratio):
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    r = int(size * radius_ratio)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=r, fill=255)
    return mask

def circle_mask(size):
    mask = Image.new("L", (size, size), 0)
    d = ImageDraw.Draw(mask)
    d.ellipse([0, 0, size - 1, size - 1], fill=255)
    return mask

def make_background(size):
    return Image.new("RGBA", (size, size), CREAM)

def make_foreground(size, scale=0.62):
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    mark_size = int(size * scale)
    mark = mark_master.resize((mark_size, mark_size), Image.LANCZOS)
    off = ((size - mark_size) // 2, (size - mark_size) // 2)
    canvas.paste(mark, off, mark)
    return canvas

def flatten_legacy(size, mask_type="round_rect", mark_scale=0.72):
    bg = make_background(size)
    mark = mark_master.resize((int(size * mark_scale), int(size * mark_scale)), Image.LANCZOS)
    canvas = bg.copy()
    off = ((size - mark.width) // 2, (size - mark.height) // 2)
    canvas.paste(mark, off, mark)
    mask = rounded_mask(size, 0.19) if mask_type == "round_rect" else circle_mask(size)
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    out.paste(canvas, (0, 0), mask)
    return out

# Adaptive icon layers (432px, full-bleed 108dp @4x)
adaptive_size = 432
bg_img = make_background(adaptive_size)
fg_img = make_foreground(adaptive_size)

d = os.path.join(RES, "mipmap-xxxhdpi")
os.makedirs(d, exist_ok=True)
bg_img.save(os.path.join(d, "ic_launcher_background.png"))
fg_img.save(os.path.join(d, "ic_launcher_foreground.png"))

densities = {
    "mipmap-mdpi": 48,
    "mipmap-hdpi": 72,
    "mipmap-xhdpi": 96,
    "mipmap-xxhdpi": 144,
    "mipmap-xxxhdpi": 192,
}
for dname, size in densities.items():
    dd = os.path.join(RES, dname)
    os.makedirs(dd, exist_ok=True)
    flatten_legacy(size, "round_rect").save(os.path.join(dd, "ic_launcher.png"))
    flatten_legacy(size, "circle").save(os.path.join(dd, "ic_launcher_round.png"))

print("done")

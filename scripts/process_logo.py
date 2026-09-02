from PIL import Image
import numpy as np
import os

SRC = "/Users/pedro/Desktop/ravanbarvar/ravanbarvar logo/ravanbarvar-logo (1).png"
RES = "/Users/pedro/ravanbarvar-patient-manager/app/src/main/res"
DRAWABLE = os.path.join(RES, "drawable")

img = Image.open(SRC).convert("RGBA")
arr = np.array(img)

# Tight crop of the icon mark only (excludes the RAVANBARVAR wordmark below it),
# with a little breathing room around the linework.
top, bottom = 136, 615
left, right = 248, 776
pad = 18
top = max(0, top - pad)
left = max(0, left - pad)
bottom = min(img.height, bottom + pad)
right = min(img.width, right + pad)

mark = img.crop((left, top, right, bottom))
# Make it square (centered) so it drops cleanly into circular/square containers.
w, h = mark.size
side = max(w, h)
square = Image.new("RGBA", (side, side), (0, 0, 0, 0))
square.paste(mark, ((side - w) // 2, (side - h) // 2), mark)

# High-res master used to derive every other asset.
master = square.resize((1024, 1024), Image.LANCZOS)
master.save(os.path.join(DRAWABLE, "logo_mark.png"))
print("mark size", mark.size, "-> square", square.size)

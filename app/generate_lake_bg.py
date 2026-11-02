#!/usr/bin/env python3
import math
import random
import subprocess
import os

width = 720
height = 1280

# Seed for reproducible, natural star distribution
random.seed(42)

# Generate stars in the sky (y from 0 to 0.62 * height)
stars = []
for _ in range(650):
    sx = random.uniform(0, width)
    sy = random.uniform(0, height * 0.60)
    brightness = random.uniform(0.35, 1.0)
    size = 1 if brightness < 0.85 else 2
    # Favor cooler starlight (white, pale cyan, subtle lavender)
    color = (
        int(230 * brightness),
        int(240 * brightness),
        int(255 * brightness)
    )
    stars.append((sx, sy, brightness, size, color))

# Horizon y position
horizon_y = height * 0.58

pixels = bytearray(width * height * 3)

def clamp(v, low=0, high=255):
    return max(low, min(high, int(v)))

for y in range(height):
    yf = y / height
    y_idx = y * width * 3

    if y < horizon_y:
        # --- SKY & CELESTIAL COSMOS ---
        # Cosmic Violet atmosphere blending into Deep Midnight Abyss
        # Top: #0A0F18 -> Mid: #1E1A3C -> Horizon: #3A265E / #4A337A
        sky_t = y / horizon_y
        
        # Cosmic nebula cloud intensity (subtle cosmic violet #7966B5 accent)
        nebula = math.sin(yf * 6.5) * 0.2 + math.cos(y * 0.012) * 0.15
        nebula_violet = max(0.0, math.sin((y - height * 0.2) * 0.008) * 0.35 + 0.15)

        r_base = 12 + 45 * (sky_t ** 1.6) + 70 * nebula_violet
        g_base = 16 + 25 * (sky_t ** 1.8) + 35 * nebula_violet
        b_base = 28 + 75 * (sky_t ** 1.4) + 95 * nebula_violet

        for x in range(width):
            xf = x / width
            idx = y_idx + x * 3

            # Milky way diagonal atmospheric band
            diag = (x * 0.7 + y * 0.5) * 0.008
            milky = max(0.0, math.sin(diag) * 0.25)

            r = r_base + 35 * milky
            g = g_base + 22 * milky
            b = b_base + 55 * milky

            # Forest tree silhouettes flanking the left and right
            left_tree_h = horizon_y - (math.sin(x * 0.08) * 22 + 45) * max(0.0, 1.0 - (x / (width * 0.32)))
            right_tree_h = horizon_y - (math.sin(x * 0.07) * 25 + 50) * max(0.0, ((x - width * 0.68) / (width * 0.32)))
            if y > left_tree_h or y > right_tree_h:
                # Dark silhouetted foliage (#0A0F16)
                r = 10
                g = 15
                b = 22

            pixels[idx] = clamp(r)
            pixels[idx + 1] = clamp(g)
            pixels[idx + 2] = clamp(b)

    else:
        # --- REFLECTIVE STARLIT LAKE & WOODEN PIER ---
        water_t = (y - horizon_y) / (height - horizon_y)
        
        # Water base color reflects sky in reverse with ripples
        ripple = math.sin(y * 0.35 + water_t * 12.0) * 8.0

        # Reflected cosmic violet and midnight abyss
        r_water = 22 + 38 * (1.0 - water_t) + ripple
        g_water = 25 + 28 * (1.0 - water_t) + ripple
        b_water = 48 + 58 * (1.0 - water_t) + ripple * 1.5

        # Wooden pier dock parameters (tapers in perspective toward the horizon)
        pier_top_w = width * 0.065
        pier_bot_w = width * 0.34
        pier_w_at_y = pier_top_w + (pier_bot_w - pier_top_w) * (water_t ** 1.25)
        pier_x_min = (width * 0.50) - (pier_w_at_y * 0.5)
        pier_x_max = (width * 0.50) + (pier_w_at_y * 0.5)

        for x in range(width):
            idx = y_idx + x * 3

            # Distant shoreline warm cabin/village lights reflection along horizon
            if y < horizon_y + 18:
                dist_light = math.sin(x * 0.12) * math.cos(x * 0.05)
                if dist_light > 0.65:
                    r_water += 65 * dist_light
                    g_water += 45 * dist_light
                    b_water += 20 * dist_light

            if pier_x_min <= x <= pier_x_max:
                # ON THE WOODEN PIER
                # Plank lines (horizontal slats)
                plank_gap = 1 if (y % int(14 + 18 * water_t)) <= 1 else 0
                
                # Wood grain and highlight
                board_frac = (x - pier_x_min) / max(1.0, pier_x_max - pier_x_min)
                center_glow = math.sin(board_frac * math.pi)

                # Weathered dark timber with starlit/violet specular sheen
                if plank_gap:
                    r = 14
                    g = 18
                    b = 26
                else:
                    r = 38 + 55 * center_glow + 20 * (1.0 - water_t)
                    g = 36 + 40 * center_glow + 15 * (1.0 - water_t)
                    b = 52 + 65 * center_glow + 25 * (1.0 - water_t)

                # Edge posts / side railings of the pier
                if abs(x - pier_x_min) < 3 or abs(x - pier_x_max) < 3:
                    r += 30
                    g += 30
                    b += 45
            else:
                # LAKE WATER
                r = r_water
                g = g_water
                b = b_water

                # Lake bank dark foliage at bottom edges
                if (x < width * 0.18 or x > width * 0.82) and water_t > 0.75:
                    edge_f = max(0.0, (water_t - 0.75) / 0.25)
                    r *= (1.0 - 0.6 * edge_f)
                    g *= (1.0 - 0.6 * edge_f)
                    b *= (1.0 - 0.6 * edge_f)

            pixels[idx] = clamp(r)
            pixels[idx + 1] = clamp(g)
            pixels[idx + 2] = clamp(b)

# Paint stars onto the sky
for sx, sy, brightness, size, (sr, sg, sb) in stars:
    cx = int(sx)
    cy = int(sy)
    if 0 <= cy < horizon_y and 0 <= cx < width:
        idx = (cy * width + cx) * 3
        # Direct star pixel
        pixels[idx] = clamp(max(pixels[idx], sr))
        pixels[idx + 1] = clamp(max(pixels[idx + 1], sg))
        pixels[idx + 2] = clamp(max(pixels[idx + 2], sb))

        # Ambient glow for larger stars
        if size > 1:
            for dx, dy in [(-1, 0), (1, 0), (0, -1), (0, 1)]:
                nx, ny = cx + dx, cy + dy
                if 0 <= ny < int(horizon_y) and 0 <= nx < width:
                    n_idx = (ny * width + nx) * 3
                    pixels[n_idx] = clamp(pixels[n_idx] + sr * 0.35)
                    pixels[n_idx + 1] = clamp(pixels[n_idx + 1] + sg * 0.35)
                    pixels[n_idx + 2] = clamp(pixels[n_idx + 2] + sb * 0.35)

# Save as PPM, then convert to optimized PNG in drawable-nodpi
ppm_path = "lake_temp.ppm"
png_path = "app/src/main/res/drawable-nodpi/img_starlit_lake_background.png"

with open(ppm_path, "wb") as f:
    f.write(f"P6\n{width} {height}\n255\n".encode("ascii"))
    f.write(pixels)

os.makedirs(os.path.dirname(png_path), exist_ok=True)
subprocess.run(["convert", ppm_path, "-quality", "92", png_path], check=True)
os.remove(ppm_path)
print(f"Generated {png_path} ({os.path.getsize(png_path)} bytes)")

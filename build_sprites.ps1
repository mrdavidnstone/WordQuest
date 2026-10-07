Add-Type -AssemblyName System.Drawing

$spritePath = "C:\Android\SpellArcade\app\src\main\assets\sprite.png"
if (-not (Test-Path $spritePath)) {
    $spritePath = "C:\Users\demol\Downloads\sprite2.png"
}

$img = [System.Drawing.Image]::FromFile($spritePath)
$baseDir = "C:\Android\SpellArcade\app\src\main\assets\sprites"

$dirs = @(
    "base_body",
    "hair",
    "shoes",
    "hats",
    "special",
    "top\blue", "top\gray", "top\green", "top\purple", "top\red",
    "bottom\blue", "bottom\gray", "bottom\green", "bottom\purple", "bottom\red"
)

foreach ($d in $dirs) {
    $p = Join-Path $baseDir $d
    if (-not (Test-Path $p)) {
        New-Item -ItemType Directory -Path $p -Force | Out-Null
    }
}

function CropAndSave([int]$row, [int]$col, [int]$totalCols, [string]$outPath) {
    $cellW = $img.Width / $totalCols
    $cellH = $img.Height / 7
    $hdrOffset = [int]($cellH * 0.12)

    $x = [int]($col * $cellW)
    $y = [int]($row * $cellH + $hdrOffset)
    $w = [int]$cellW
    $h = [int]($cellH - $hdrOffset)

    $rect = New-Object System.Drawing.Rectangle($x, $y, $w, $h)
    $bmp = New-Object System.Drawing.Bitmap($w, $h)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.DrawImage($img, (New-Object System.Drawing.Rectangle(0,0,$w,$h)), $rect, [System.Drawing.GraphicsUnit]::Pixel)
    $g.Dispose()

    for ($px = 0; $px -lt $bmp.Width; $px++) {
        for ($py = 0; $py -lt $bmp.Height; $py++) {
            $c = $bmp.GetPixel($px, $py)
            if (($c.R -ge 230 -and $c.G -ge 230 -and $c.B -ge 230) -or ($c.R -ge 200 -and $c.R -le 218 -and $c.G -ge 200 -and $c.G -le 218 -and $c.B -ge 200 -and $c.B -le 218)) {
                $bmp.SetPixel($px, $py, [System.Drawing.Color]::Transparent)
            }
        }
    }

    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
}

# 1. Base Body
CropAndSave 0 0 9 (Join-Path $baseDir "base_body\body_01.png")
CropAndSave 0 1 9 (Join-Path $baseDir "base_body\body_02.png")

# 2. Hair & Special Hat Hair
CropAndSave 1 2 9 (Join-Path $baseDir "hair\spiky.png")
CropAndSave 1 3 9 (Join-Path $baseDir "hair\side_part.png")
CropAndSave 1 4 9 (Join-Path $baseDir "hair\curly.png")
CropAndSave 1 5 9 (Join-Path $baseDir "hair\undercut.png")
CropAndSave 1 6 9 (Join-Path $baseDir "hair\ponytail.png")
CropAndSave 1 7 9 (Join-Path $baseDir "hair\braids.png")
CropAndSave 1 8 9 (Join-Path $baseDir "hair\bun.png")
CropAndSave 1 2 9 (Join-Path $baseDir "special\hat_hair_spiky_brown.png")

# 3. Hats
CropAndSave 2 7 10 (Join-Path $baseDir "hats\hat_baseball.png")
CropAndSave 2 7 10 (Join-Path $baseDir "hats\baseball_red.png")

# 4. Top (Hoodie, T-Shirt, Jacket, Long Sleeve, Sweater in 5 Colors)
$colors = @("blue", "red", "green", "gray", "purple")
for ($i = 0; $i -lt $colors.Length; $i++) {
    $c = $colors[$i]
    CropAndSave 3 $i 10 (Join-Path $baseDir "top\$c\hoodie.png")
    CropAndSave 3 ($i+5) 10 (Join-Path $baseDir "top\$c\t_shirt.png")
    CropAndSave 4 $i 10 (Join-Path $baseDir "top\$c\jacket.png")
    CropAndSave 4 ($i+5) 10 (Join-Path $baseDir "top\$c\long_sleeve.png")
    CropAndSave 5 $i 10 (Join-Path $baseDir "top\$c\sweater.png")
}

# 5. Bottom (Jeans, Cargo Pants, Shorts, Joggers in Colors)
CropAndSave 5 5 10 (Join-Path $baseDir "bottom\blue\jeans.png")
CropAndSave 5 6 10 (Join-Path $baseDir "bottom\gray\jeans.png")
CropAndSave 5 7 10 (Join-Path $baseDir "bottom\purple\jeans.png")
CropAndSave 5 8 10 (Join-Path $baseDir "bottom\blue\cargo_pants.png")
CropAndSave 5 9 10 (Join-Path $baseDir "bottom\gray\cargo_pants.png")

CropAndSave 6 0 10 (Join-Path $baseDir "bottom\purple\cargo_pants.png")
CropAndSave 6 1 10 (Join-Path $baseDir "bottom\blue\shorts.png")
CropAndSave 6 2 10 (Join-Path $baseDir "bottom\gray\shorts.png")
CropAndSave 6 3 10 (Join-Path $baseDir "bottom\purple\shorts.png")
CropAndSave 6 4 10 (Join-Path $baseDir "bottom\blue\joggers.png")
CropAndSave 6 5 10 (Join-Path $baseDir "bottom\gray\joggers.png")
CropAndSave 6 6 10 (Join-Path $baseDir "bottom\purple\joggers.png")

# 6. Shoes
CropAndSave 6 7 10 (Join-Path $baseDir "shoes\shoes_01.png")
CropAndSave 6 8 10 (Join-Path $baseDir "shoes\shoes_02.png")
CropAndSave 6 9 10 (Join-Path $baseDir "shoes\shoes_03.png")

$img.Dispose()
Write-Host "SPECIAL HAT HAIR & SPRITES CREATED SUCCESSFULLY!"

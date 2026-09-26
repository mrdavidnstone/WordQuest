$baseDir = "C:\Android\SpellingGames\app\src\main\assets\sprites"

$dirs = @("base_body", "top\gray", "bottom\khaki", "shoes", "special", "hats")
foreach ($d in $dirs) {
    $p = Join-Path $baseDir $d
    if (-not (Test-Path $p)) { New-Item -ItemType Directory -Path $p -Force | Out-Null }
}

Copy-Item (Join-Path $baseDir "top\gray\hoodie.png") (Join-Path $baseDir "top\gray\top_hoodie_gray.png") -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $baseDir "bottom\gray\cargo_pants.png") (Join-Path $baseDir "bottom\khaki\bottom_cargo_khaki.png") -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $baseDir "shoes\shoes_01.png") (Join-Path $baseDir "shoes\shoes_blue.png") -Force -ErrorAction SilentlyContinue
Copy-Item (Join-Path $baseDir "hats\baseball_red.png") (Join-Path $baseDir "hats\cap_red.png") -Force -ErrorAction SilentlyContinue

Write-Host "PNG ALIASES CREATED SUCCESSFULLY!"

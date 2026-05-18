Add-Type -AssemblyName System.Drawing

function New-HexColor {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Hex,
        [int]$Alpha = 255
    )

    $value = $Hex.TrimStart('#')
    if ($value.Length -ne 6) {
        throw "Expected a 6-digit hex color, got '$Hex'."
    }

    $r = [Convert]::ToInt32($value.Substring(0, 2), 16)
    $g = [Convert]::ToInt32($value.Substring(2, 2), 16)
    $b = [Convert]::ToInt32($value.Substring(4, 2), 16)
    return [System.Drawing.Color]::FromArgb($Alpha, $r, $g, $b)
}

function New-RoundedRectPath {
    param(
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height,
        [float]$Radius
    )

    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $diameter = $Radius * 2

    $path.AddArc($X, $Y, $diameter, $diameter, 180, 90)
    $path.AddArc($X + $Width - $diameter, $Y, $diameter, $diameter, 270, 90)
    $path.AddArc($X + $Width - $diameter, $Y + $Height - $diameter, $diameter, $diameter, 0, 90)
    $path.AddArc($X, $Y + $Height - $diameter, $diameter, $diameter, 90, 90)
    $path.CloseFigure()

    return $path
}

function New-PickPath {
    param(
        [float]$X,
        [float]$Y,
        [float]$Size
    )

    $cx = $X + ($Size * 0.5)
    $top = $Y + ($Size * 0.06)
    $bottom = $Y + ($Size * 0.94)
    $left = $X + ($Size * 0.10)
    $right = $X + ($Size * 0.90)
    $upperY = $Y + ($Size * 0.30)
    $midY = $Y + ($Size * 0.58)

    $path = New-Object System.Drawing.Drawing2D.GraphicsPath
    $path.StartFigure()
    $path.AddBezier(
        [System.Drawing.PointF]::new($cx, $top),
        [System.Drawing.PointF]::new($X + ($Size * 0.76), $top),
        [System.Drawing.PointF]::new($right, $Y + ($Size * 0.14)),
        [System.Drawing.PointF]::new($right, $upperY)
    )
    $path.AddBezier(
        [System.Drawing.PointF]::new($right, $upperY),
        [System.Drawing.PointF]::new($right, $midY),
        [System.Drawing.PointF]::new($X + ($Size * 0.70), $bottom),
        [System.Drawing.PointF]::new($cx, $bottom)
    )
    $path.AddBezier(
        [System.Drawing.PointF]::new($cx, $bottom),
        [System.Drawing.PointF]::new($X + ($Size * 0.30), $bottom),
        [System.Drawing.PointF]::new($left, $midY),
        [System.Drawing.PointF]::new($left, $upperY)
    )
    $path.AddBezier(
        [System.Drawing.PointF]::new($left, $upperY),
        [System.Drawing.PointF]::new($left, $Y + ($Size * 0.14)),
        [System.Drawing.PointF]::new($X + ($Size * 0.24), $top),
        [System.Drawing.PointF]::new($cx, $top)
    )
    $path.CloseFigure()

    return $path
}

function Fill-Glow {
    param(
        [System.Drawing.Graphics]$Graphics,
        [float]$X,
        [float]$Y,
        [float]$Width,
        [float]$Height,
        [System.Drawing.Color]$CenterColor,
        [System.Drawing.Color]$EdgeColor
    )

    $ellipsePath = New-Object System.Drawing.Drawing2D.GraphicsPath
    $ellipsePath.AddEllipse($X, $Y, $Width, $Height)
    $brush = New-Object System.Drawing.Drawing2D.PathGradientBrush($ellipsePath)
    $brush.CenterColor = $CenterColor
    $brush.SurroundColors = [System.Drawing.Color[]]@($EdgeColor)
    $Graphics.FillEllipse($brush, $X, $Y, $Width, $Height)
    $brush.Dispose()
    $ellipsePath.Dispose()
}

function Draw-SymbolLayers {
    param(
        [System.Drawing.Graphics]$Graphics,
        [float]$X,
        [float]$Y,
        [float]$Size
    )

    $whiteStrong = New-HexColor '#F3F8FF'
    $whiteSoft = New-HexColor '#E6F1FF' 190
    $whiteBorder = New-HexColor '#FFFFFF' 120
    $pegGlow = New-HexColor '#B9FFD8' 80
    $headFill = New-HexColor '#D7E7FF' 30
    $headStroke = New-HexColor '#F5FAFF' 160

    $forkWidth = $Size * 0.09
    $forkHeight = $Size * 0.28
    $prongOffset = $Size * 0.14
    $prongY = $Y + ($Size * 0.15)
    $stemY = $Y + ($Size * 0.25)
    $stemHeight = $Size * 0.33
    $centerX = $X + ($Size * 0.5)

    foreach ($offset in @(-$prongOffset, $prongOffset)) {
        $prongPath = New-RoundedRectPath -X ($centerX + $offset - ($forkWidth / 2)) -Y $prongY -Width $forkWidth -Height $forkHeight -Radius ($forkWidth * 0.45)
        $Graphics.FillPath((New-Object System.Drawing.SolidBrush($whiteStrong)), $prongPath)
        $prongPath.Dispose()
    }

    $stemPath = New-RoundedRectPath -X ($centerX - ($forkWidth / 2)) -Y $stemY -Width $forkWidth -Height $stemHeight -Radius ($forkWidth * 0.45)
    $Graphics.FillPath((New-Object System.Drawing.SolidBrush($whiteStrong)), $stemPath)
    $stemPath.Dispose()

    $stringPenCenter = New-Object System.Drawing.Pen($whiteStrong, [Math]::Max(1.4, $Size * 0.014))
    $stringPenCenter.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $stringPenCenter.EndCap = [System.Drawing.Drawing2D.LineCap]::Round
    $stringPenSide = New-Object System.Drawing.Pen($whiteSoft, [Math]::Max(1.0, $Size * 0.010))
    $stringPenSide.StartCap = [System.Drawing.Drawing2D.LineCap]::Round
    $stringPenSide.EndCap = [System.Drawing.Drawing2D.LineCap]::Round

    $stringTop = $Y + ($Size * 0.56)
    $stringBottom = $Y + ($Size * 0.73)
    $Graphics.DrawLine($stringPenCenter, $centerX, $stringTop, $centerX, $stringBottom)
    $Graphics.DrawLine($stringPenSide, $centerX - ($Size * 0.07), $stringTop, $centerX - ($Size * 0.07), $stringBottom)
    $Graphics.DrawLine($stringPenSide, $centerX + ($Size * 0.07), $stringTop, $centerX + ($Size * 0.07), $stringBottom)

    $stringPenCenter.Dispose()
    $stringPenSide.Dispose()

    $headWidth = $Size * 0.46
    $headHeight = $Size * 0.17
    $headX = $centerX - ($headWidth / 2)
    $headY = $Y + ($Size * 0.70)
    $headPath = New-RoundedRectPath -X $headX -Y $headY -Width $headWidth -Height $headHeight -Radius ($Size * 0.07)
    $Graphics.FillPath((New-Object System.Drawing.SolidBrush($headFill)), $headPath)
    $Graphics.DrawPath((New-Object System.Drawing.Pen($headStroke, [Math]::Max(1.5, $Size * 0.012))), $headPath)
    $headPath.Dispose()

    $pegRadius = $Size * 0.034
    $pegPositions = @(
        [System.Drawing.PointF]::new($headX + ($headWidth * 0.23), $headY + ($headHeight * 0.38)),
        [System.Drawing.PointF]::new($headX + ($headWidth * 0.77), $headY + ($headHeight * 0.38)),
        [System.Drawing.PointF]::new($headX + ($headWidth * 0.50), $headY + ($headHeight * 0.62))
    )
    foreach ($peg in $pegPositions) {
        $pegBrush = New-Object System.Drawing.SolidBrush($whiteStrong)
        $glowBrush = New-Object System.Drawing.SolidBrush($pegGlow)
        $Graphics.FillEllipse($glowBrush, $peg.X - ($pegRadius * 1.45), $peg.Y - ($pegRadius * 1.45), $pegRadius * 2.9, $pegRadius * 2.9)
        $Graphics.FillEllipse($pegBrush, $peg.X - $pegRadius, $peg.Y - $pegRadius, $pegRadius * 2, $pegRadius * 2)
        $glowBrush.Dispose()
        $pegBrush.Dispose()
    }
}

function Draw-IconCanvas {
    param(
        [System.Drawing.Graphics]$Graphics,
        [int]$Size,
        [bool]$IncludeSquareBackground
    )

    $Graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $Graphics.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
    $Graphics.PixelOffsetMode = [System.Drawing.Drawing2D.PixelOffsetMode]::HighQuality
    $Graphics.CompositingQuality = [System.Drawing.Drawing2D.CompositingQuality]::HighQuality

    if ($IncludeSquareBackground) {
        $rect = New-Object System.Drawing.RectangleF(0, 0, $Size, $Size)
        $bgBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
            [System.Drawing.PointF]::new(0, 0),
            [System.Drawing.PointF]::new(0, $Size),
            (New-HexColor '#1A2442'),
            (New-HexColor '#040810')
        )
        $blend = New-Object System.Drawing.Drawing2D.ColorBlend
        $blend.Colors = [System.Drawing.Color[]]@(
            (New-HexColor '#22325D'),
            (New-HexColor '#10192C'),
            (New-HexColor '#050912')
        )
        $blend.Positions = [single[]]@(0.0, 0.45, 1.0)
        $bgBrush.InterpolationColors = $blend
        $Graphics.FillRectangle($bgBrush, $rect)
        $bgBrush.Dispose()

        Fill-Glow -Graphics $Graphics -X ($Size * 0.08) -Y ($Size * 0.03) -Width ($Size * 0.62) -Height ($Size * 0.46) -CenterColor (New-HexColor '#65D7FF' 40) -EdgeColor (New-HexColor '#65D7FF' 0)
        Fill-Glow -Graphics $Graphics -X ($Size * 0.56) -Y ($Size * 0.52) -Width ($Size * 0.38) -Height ($Size * 0.28) -CenterColor (New-HexColor '#00FF99' 18) -EdgeColor (New-HexColor '#00FF99' 0)
    }

    $pickSize = if ($IncludeSquareBackground) { $Size * 0.70 } else { $Size * 0.74 }
    $pickX = ($Size - $pickSize) / 2
    $pickY = if ($IncludeSquareBackground) { $Size * 0.13 } else { $Size * 0.10 }
    $pickPath = New-PickPath -X $pickX -Y $pickY -Size $pickSize

    $pickBrush = New-Object System.Drawing.Drawing2D.LinearGradientBrush(
        [System.Drawing.PointF]::new($pickX, $pickY),
        [System.Drawing.PointF]::new($pickX, $pickY + $pickSize),
        (New-HexColor '#2D447B'),
        (New-HexColor '#08101C')
    )
    $pickBlend = New-Object System.Drawing.Drawing2D.ColorBlend
    $pickBlend.Colors = [System.Drawing.Color[]]@(
        (New-HexColor '#354F8B'),
        (New-HexColor '#18243F'),
        (New-HexColor '#09111E')
    )
    $pickBlend.Positions = [single[]]@(0.0, 0.48, 1.0)
    $pickBrush.InterpolationColors = $pickBlend
    $Graphics.FillPath($pickBrush, $pickPath)
    $pickBrush.Dispose()

    Fill-Glow -Graphics $Graphics -X ($pickX + ($pickSize * 0.08)) -Y ($pickY + ($pickSize * 0.02)) -Width ($pickSize * 0.48) -Height ($pickSize * 0.28) -CenterColor (New-HexColor '#84E8FF' 55) -EdgeColor (New-HexColor '#84E8FF' 0)

    $strokeOuter = New-Object System.Drawing.Pen((New-HexColor '#86E3FF' 120), [Math]::Max(1.5, $pickSize * 0.015))
    $strokeInner = New-Object System.Drawing.Pen((New-HexColor '#FFFFFF' 40), [Math]::Max(1.0, $pickSize * 0.006))
    $Graphics.DrawPath($strokeOuter, $pickPath)

    $innerPath = New-PickPath -X ($pickX + ($pickSize * 0.04)) -Y ($pickY + ($pickSize * 0.04)) -Size ($pickSize * 0.92)
    $Graphics.DrawPath($strokeInner, $innerPath)
    $innerPath.Dispose()
    $strokeOuter.Dispose()
    $strokeInner.Dispose()

    Draw-SymbolLayers -Graphics $Graphics -X $pickX -Y $pickY -Size $pickSize
    $pickPath.Dispose()
}

function Save-Bitmap {
    param(
        [System.Drawing.Bitmap]$Bitmap,
        [string]$Path
    )

    $directory = Split-Path -Parent $Path
    if (-not (Test-Path $directory)) {
        New-Item -ItemType Directory -Path $directory -Force | Out-Null
    }

    $Bitmap.Save($Path, [System.Drawing.Imaging.ImageFormat]::Png)
}

function New-IconBitmap {
    param(
        [int]$Size,
        [bool]$IncludeSquareBackground
    )

    $bitmap = New-Object System.Drawing.Bitmap($Size, $Size)
    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
    $graphics.Clear([System.Drawing.Color]::Transparent)
    Draw-IconCanvas -Graphics $graphics -Size $Size -IncludeSquareBackground:$IncludeSquareBackground
    $graphics.Dispose()
    return $bitmap
}

$root = Resolve-Path (Join-Path $PSScriptRoot '..')

$legacySizes = @{
    'mdpi'    = 48
    'hdpi'    = 72
    'xhdpi'   = 96
    'xxhdpi'  = 144
    'xxxhdpi' = 192
}

$foregroundSizes = @{
    'mdpi'    = 108
    'hdpi'    = 162
    'xhdpi'   = 216
    'xxhdpi'  = 324
    'xxxhdpi' = 432
}

foreach ($density in $legacySizes.Keys) {
    $iconBitmap = New-IconBitmap -Size $legacySizes[$density] -IncludeSquareBackground:$true
    $legacyPath = Join-Path $root "app/src/main/res/mipmap-$density/ic_launcher.png"
    $roundPath = Join-Path $root "app/src/main/res/mipmap-$density/ic_launcher_round.png"
    Save-Bitmap -Bitmap $iconBitmap -Path $legacyPath
    Save-Bitmap -Bitmap $iconBitmap -Path $roundPath
    $iconBitmap.Dispose()

    $foregroundBitmap = New-IconBitmap -Size $foregroundSizes[$density] -IncludeSquareBackground:$false
    $foregroundPath = Join-Path $root "app/src/main/res/mipmap-$density/ic_launcher_foreground.png"
    Save-Bitmap -Bitmap $foregroundBitmap -Path $foregroundPath
    $foregroundBitmap.Dispose()
}

$storeBitmap = New-IconBitmap -Size 512 -IncludeSquareBackground:$true
Save-Bitmap -Bitmap $storeBitmap -Path (Join-Path $root 'app/src/main/res/drawable/app_icon.png')
$storeBitmap.Dispose()

Write-Output 'App icon assets generated.'

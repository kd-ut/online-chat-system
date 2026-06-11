#!/bin/bash
# ============================================================
# WebChat 企业级技术文档 Markdown → Word (.docx) 转换工具
# ============================================================
# 依赖: pandoc (必需)
# 安装: https://pandoc.org/installing.html
# ============================================================

DOCS_DIR="$(cd "$(dirname "$0")" && pwd)"
FILES=(
    "01-requirements.md"
    "02-design.md"
    "03-database.md"
    "04-api.md"
    "05-user-guide.md"
)

TITLE_MAP=(
    "需求分析规格说明书"
    "系统设计说明书"
    "数据库设计说明书"
    "接口设计说明书"
    "用户操作手册"
)

echo "=========================================="
echo " WebChat 企业级文档 .docx 转换工具"
echo "=========================================="
echo ""

# 检查 pandoc
if ! command -v pandoc &>/dev/null; then
    echo "[错误] pandoc 未安装！"
    echo ""
    echo "请先安装 pandoc:"
    echo "  Ubuntu/Debian: sudo apt install pandoc"
    echo "  macOS:         brew install pandoc"
    echo "  Windows:       https://pandoc.org/installing.html"
    echo ""
    echo "安装后重新运行此脚本。"
    exit 1
fi

PANDOC_VER=$(pandoc --version | head -1)
echo "  pandoc 版本: $PANDOC_VER"
echo ""

# 检查输入文件
MISSING=0
for file in "${FILES[@]}"; do
    if [ ! -f "$DOCS_DIR/$file" ]; then
        echo "  [缺失] $file"
        MISSING=1
    fi
done

if [ $MISSING -eq 1 ]; then
    echo ""
    echo "[错误] 部分源文件缺失，请确认 docs/ 目录完整。"
    exit 1
fi

echo "  找到 ${#FILES[@]} 个源文件"
echo ""

# 开始转换
echo "开始转换..."

for i in "${!FILES[@]}"; do
    file="${FILES[$i]}"
    title="${TITLE_MAP[$i]}"
    input_path="$DOCS_DIR/$file"
    output_path="${input_path%.md}.docx"

    echo ""
    echo "  [${i}/$((${#FILES[@]}-1))] 转换中: $file"
    echo "         标题: $title"

    pandoc "$input_path" \
        --from markdown \
        --to docx \
        --output "$output_path" \
        --resource-path="$DOCS_DIR" \
        --metadata title="WebChat - $title" \
        --metadata author="WebChat Architecture Team" \
        --metadata date="$(date +%Y-%m-%d)" \
        --wrap=none \
        --highlight-style=tango \
        --toc \
        --toc-depth=3 \
        --number-sections \
        --reference-doc="$DOCS_DIR/reference.docx" 2>/dev/null || \
    pandoc "$input_path" \
        --from markdown \
        --to docx \
        --output "$output_path" \
        --resource-path="$DOCS_DIR" \
        --metadata title="WebChat - $title" \
        --metadata author="WebChat Architecture Team" \
        --metadata date="$(date +%Y-%m-%d)" \
        --wrap=none \
        --highlight-style=tango \
        --toc \
        --toc-depth=3 \
        --number-sections

    if [ $? -eq 0 ]; then
        SIZE=$(du -h "$output_path" | cut -f1)
        echo "         ✓ 完成! ($SIZE)"
    else
        echo "         ✗ 失败!"
    fi
done

echo ""
echo "=========================================="
echo " 转换完成！"
echo "=========================================="
echo ""
echo " 输出文件:"
for i in "${!FILES[@]}"; do
    file="${FILES[$i]}"
    docx_path="${file%.md}.docx"
    if [ -f "$DOCS_DIR/$docx_path" ]; then
        echo "    📄 $DOCS_DIR/$docx_path"
    fi
done
echo ""
echo "=========================================="
echo " Mermaid 图表处理说明:"
echo "=========================================="
echo ""
echo "  本文档包含大量 Mermaid 图表代码块（类图、时序图、"
echo "  流程图、ER 图、状态图等）。pandoc 无法直接渲染"
echo "  Mermaid，需要手动处理："
echo ""
echo "  方法一（推荐）："
echo "    1. 打开 Word 文档"
echo "    2. 搜索 '请在 Word 中通过插件或在线工具'"
echo "    3. 复制下方的 Mermaid 代码块"
echo "    4. 访问 https://mermaid.live 在线渲染"
echo "    5. 导出为 PNG/SVG，插入 Word 对应位置"
echo ""
echo "  方法二（Mermaid 插件）："
echo "    在 Word 中安装 Mermaid 插件（如 Mermaid Chart）"
echo "    直接将代码块粘贴到插件中渲染"
echo ""
echo "  方法三（批量处理）："
echo "    使用 mermaid-cli 批量渲染："
echo "    npx @mermaid-js/mermaid-cli mmdc -i input.mmd -o output.png"
echo "=========================================="

import os

import markdown
from xhtml2pdf import pisa


def convert_md_to_pdf(source_md: str, output_pdf: str) -> None:
    if not os.path.exists(source_md):
        raise FileNotFoundError(f"Source Markdown not found: {source_md}")

    with open(source_md, "r", encoding="utf-8") as f:
        text = f.read()

    html_body = markdown.markdown(
        text,
        extensions=[
            "extra",  # tables, fenced code blocks, etc.
        ],
        output_format="html5",
    )

    # Note: xhtml2pdf supports only a subset of CSS. Avoid advanced @page margin boxes.
    full_html = f"""<!DOCTYPE html>
<html>
<head>
  <meta charset=\"UTF-8\" />
  <style>
    @page {{
      size: A4;
      margin: 2cm;
    }}

    body {{
      font-family: Helvetica, Arial, sans-serif;
      font-size: 11pt;
      line-height: 1.5;
      color: #222;
    }}

    h1 {{ font-size: 20pt; margin: 18pt 0 10pt; }}
    h2 {{ font-size: 14pt; margin: 16pt 0 8pt; }}
    h3 {{ font-size: 12pt; margin: 14pt 0 6pt; }}

    code, pre {{
      font-family: Courier New, monospace;
      font-size: 9pt;
    }}

    pre {{
      background: #f4f4f4;
      padding: 8pt;
      white-space: pre-wrap;
      border: 1px solid #ddd;
    }}

    table {{
      border-collapse: collapse;
      width: 100%;
      margin: 8pt 0;
    }}
    th, td {{
      border: 1px solid #999;
      padding: 6pt;
    }}
    th {{ background: #eee; }}

    a {{ color: #0645ad; text-decoration: none; }}
  </style>
</head>
<body>
{html_body}
</body>
</html>"""

    with open(output_pdf, "wb") as out:
        status = pisa.CreatePDF(full_html, dest=out)

    if status.err:
        raise RuntimeError("PDF generation failed (xhtml2pdf returned errors).")


if __name__ == "__main__":
    convert_md_to_pdf("RAPPORT.md", "RAPPORT.pdf")
    print("PDF generated: RAPPORT.pdf")

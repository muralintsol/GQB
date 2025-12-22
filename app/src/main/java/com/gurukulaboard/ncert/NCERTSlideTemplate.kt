package com.gurukulaboard.ncert

object NCERTSlideTemplate {
    
    /**
     * Generate HTML template for slides
     */
    fun generateSlideHTML(
        title: String,
        slides: List<SlideContent>,
        subject: String,
        chapter: String
    ): String {
        val slidesHTML = slides.joinToString("\n") { slide ->
            when (slide.type) {
                SlideType.TITLE -> generateTitleSlide(slide.content, subject, chapter)
                SlideType.CONTENT -> generateContentSlide(slide.title, slide.content, slide.bulletPoints)
                SlideType.SUMMARY -> generateSummarySlide(slide.content, slide.bulletPoints)
            }
        }
        
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>$title</title>
    <style>
        ${getSlideCSS()}
    </style>
</head>
<body>
    <div class="slides-container">
        $slidesHTML
    </div>
    <script>
        ${getSlideJavaScript()}
    </script>
</body>
</html>
        """.trimIndent()
    }
    
    private fun generateTitleSlide(content: String, subject: String, chapter: String): String {
        return """
        <div class="slide title-slide">
            <h1 class="slide-title">$content</h1>
            <h2 class="slide-subtitle">$subject</h2>
            <h3 class="slide-chapter">$chapter</h3>
        </div>
        """.trimIndent()
    }
    
    private fun generateContentSlide(title: String, content: String, bulletPoints: List<String>?): String {
        val bulletsHTML = bulletPoints?.joinToString("\n") { "<li>$it</li>" } ?: ""
        val bulletsSection = if (bulletsHTML.isNotEmpty()) {
            "<ul class='bullet-list'>$bulletsHTML</ul>"
        } else ""
        
        return """
        <div class="slide content-slide">
            <h2 class="slide-heading">$title</h2>
            <div class="slide-content">
                <p>$content</p>
                $bulletsSection
            </div>
        </div>
        """.trimIndent()
    }
    
    private fun generateSummarySlide(content: String, bulletPoints: List<String>?): String {
        val bulletsHTML = bulletPoints?.joinToString("\n") { "<li>$it</li>" } ?: ""
        
        return """
        <div class="slide summary-slide">
            <h2 class="slide-heading">Summary</h2>
            <div class="slide-content">
                <p>$content</p>
                <ul class="bullet-list">$bulletsHTML</ul>
            </div>
        </div>
        """.trimIndent()
    }
    
    private fun getSlideCSS(): String {
        return """
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: 'Arial', 'Helvetica', sans-serif;
            background: #f0f0f0;
            padding: 20px;
        }
        
        .slides-container {
            max-width: 1200px;
            margin: 0 auto;
        }
        
        .slide {
            background: white;
            margin-bottom: 30px;
            padding: 60px;
            border-radius: 8px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            min-height: 600px;
            display: flex;
            flex-direction: column;
            justify-content: center;
            page-break-after: always;
        }
        
        .title-slide {
            text-align: center;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .slide-title {
            font-size: 48px;
            font-weight: bold;
            margin-bottom: 20px;
        }
        
        .slide-subtitle {
            font-size: 32px;
            margin-bottom: 10px;
            opacity: 0.9;
        }
        
        .slide-chapter {
            font-size: 24px;
            opacity: 0.8;
        }
        
        .content-slide, .summary-slide {
            background: white;
        }
        
        .slide-heading {
            font-size: 36px;
            color: #333;
            margin-bottom: 30px;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
        }
        
        .slide-content {
            font-size: 20px;
            line-height: 1.6;
            color: #555;
        }
        
        .slide-content p {
            margin-bottom: 20px;
        }
        
        .bullet-list {
            margin-left: 30px;
            margin-top: 20px;
        }
        
        .bullet-list li {
            margin-bottom: 15px;
            font-size: 18px;
            line-height: 1.5;
        }
        
        @media print {
            .slide {
                page-break-after: always;
                margin-bottom: 0;
            }
        }
        """.trimIndent()
    }
    
    private fun getSlideJavaScript(): String {
        return """
        // Navigation between slides (optional)
        let currentSlide = 0;
        const slides = document.querySelectorAll('.slide');
        
        function showSlide(index) {
            slides.forEach((slide, i) => {
                slide.style.display = i === index ? 'flex' : 'none';
            });
        }
        
        // Keyboard navigation
        document.addEventListener('keydown', (e) => {
            if (e.key === 'ArrowRight' && currentSlide < slides.length - 1) {
                currentSlide++;
                showSlide(currentSlide);
            } else if (e.key === 'ArrowLeft' && currentSlide > 0) {
                currentSlide--;
                showSlide(currentSlide);
            }
        });
        
        // Show all slides by default (for printing)
        slides.forEach(slide => slide.style.display = 'flex');
        """.trimIndent()
    }
}

data class SlideContent(
    val type: SlideType,
    val title: String = "",
    val content: String = "",
    val bulletPoints: List<String>? = null
)

enum class SlideType {
    TITLE,
    CONTENT,
    SUMMARY
}


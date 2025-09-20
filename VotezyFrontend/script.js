document.addEventListener('DOMContentLoaded', () => {
    const imageElements = [
        document.getElementById('image1'),
        document.getElementById('image2'),
        document.getElementById('image3')
    ];

    // Common images for everyone
    const images = [
        "https://www.grantthornton.in/globalassets/1.-member-firms/india/new-homepage/media/1.-hero-banners_repeat-visits/1440x600px_hero_banner_adobestock_594986042.jpg",
        "https://www.definedgesecurities.com/wp-content/uploads/2024/05/64563bb6-e396-4bd7-8d7c-707a52cc86cd-scaled.jpg",
        "https://akm-img-a-in.tosshub.com/indiatoday/images/story/202403/lok-sabha-polls-200325600-16x9_0.png?VersionId=ScTJWGIwZ3k6Cb10d5AUdn5SOuFpmfpo&size=690:388"
    ];
    imageElements.forEach((img, index) => {
        img.src = images[index];
        img.alt = `Election image ${index + 1}`;
    });
});
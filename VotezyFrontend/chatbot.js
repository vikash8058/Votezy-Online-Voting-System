// (Removed duplicate/old chatbot popup logic that could show behind panels)
// Chatbot popup logic with AI avatar and greeting
document.addEventListener('DOMContentLoaded', function() {
    // Remove any old chatbot popup
    const oldPopup = document.querySelector('.chatbot-popup');
    if (oldPopup) oldPopup.remove();

    const chatbotBtn = document.getElementById('chatbot-btn');
    if (!chatbotBtn) return;

    let popup = null;
    let chatbotPopup, chatbotClose, chatbotForm, chatbotInput, chatbotMessages;

    function createPopup() {
        popup = document.createElement('div');
        popup.className = 'chatbot-popup';
        popup.innerHTML = `
            <div class="chatbot-header" style="justify-content:center;">
                <span style="font-weight:600;font-size:1.1rem;">Votezy AI Assistant</span>
                <button class="chatbot-close" title="Close">&times;</button>
            </div>
            <div class="chatbot-messages">
                <div class="chatbot-message bot">👋 Hi! I'm Votezy AI. How can I help you today?</div>
            </div>
            <form class="chatbot-input" autocomplete="off">
                <input type="text" placeholder="Type your message..." required />
                <button type="submit">Send</button>
            </form>
        `;
        document.body.appendChild(popup);
        chatbotPopup = popup;
        chatbotClose = chatbotPopup.querySelector('.chatbot-close');
        chatbotForm = chatbotPopup.querySelector('.chatbot-input');
        chatbotInput = chatbotForm.querySelector('input');
        chatbotMessages = chatbotPopup.querySelector('.chatbot-messages');

        chatbotClose.addEventListener('click', () => {
            chatbotPopup.classList.remove('active');
        });

        chatbotForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const userMsg = chatbotInput.value.trim();
            if (!userMsg) return;
            const userDiv = document.createElement('div');
            userDiv.className = 'chatbot-message user';
            userDiv.textContent = userMsg;
            chatbotMessages.appendChild(userDiv);
            chatbotInput.value = '';
            chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
            setTimeout(() => {
                const botDiv = document.createElement('div');
                botDiv.className = 'chatbot-message bot';
                botDiv.textContent = 'Thank you for your message!';
                chatbotMessages.appendChild(botDiv);
                chatbotMessages.scrollTop = chatbotMessages.scrollHeight;
            }, 700);
        });
    }

    chatbotBtn.addEventListener('click', () => {
        if (!popup) {
            createPopup();
        }
        popup.classList.add('active');
        const input = popup.querySelector('input');
        if (input) input.focus();
    });
});

// Navbar behavior - Dynamic logo link based on session
document.addEventListener('DOMContentLoaded', () => {
    setupDynamicLogo();
});

function setupDynamicLogo() {
    // Try to find logo (in navbar) or auth-logo (in login/register pages)
    const logo = document.querySelector('.logo') || document.querySelector('.auth-logo');
    if (!logo) return;

    // Make cursor a pointer to show it's clickable
    logo.style.cursor = 'pointer';

    // Add click event listener to intercept clicks
    logo.addEventListener('click', (e) => {
        e.preventDefault(); // Prevent default link behavior

        // Check if user is logged in
        const userSession = getCookie('JSESSIONID');
        const userRole = localStorage.getItem('userRole');

        let destination = 'index.html'; // Default for not logged in

        if (userSession && userRole) {
            // User is logged in - redirect to appropriate dashboard
            if (userRole === 'ADMIN') {
                destination = 'admin-dashboard.html';
            } else {
                destination = 'user-dashboard.html';
            }
        }

        // Navigate to destination
        window.location.href = destination;
    });
}

// Helper function to get cookies
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}

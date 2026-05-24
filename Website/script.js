const DEFAULT_DATA = {
    title: "Our WhatsApp Chat",
    chatDates: {
        start: "01.01.23",
        end: "31.12.24"
    },
    participants: { left: "Alina", right: "Max" },
    replyTimes: { left: 44, right: 43 },
    topEmojis: [
        { emoji: "😂", value: 666 },
        { emoji: "😢", value: 420 },
        { emoji: "😞", value: 345 },
        { emoji: "😭", value: 111 },
        { emoji: "😡", value: 69 },
        { emoji: "😔", value: 42 }
    ],
    mostUsedWords: ["love", "okay", "babe", "stop", "way", "can't", "today", "im", "literally"],
    records: [
        { label: "Most messages in one day", value: "63" },
        { label: "Longest time apart", value: "12hrs" },
        { label: "Fastest reply", value: "5 seconds" }
    ],
    timeSlotsMorning: [
        { hour: "0:00", count: 345 }, { hour: "1:00", count: 59 },
        { hour: "2:00", count: 328 }, { hour: "3:00", count: 326 },
        { hour: "4:00", count: 337 }, { hour: "5:00", count: 634 }
    ],
    timeSlotsAfternoon: [
        { hour: "13:00", count: 1654 }, { hour: "14:00", count: 2188 },
        { hour: "15:00", count: 1939 }, { hour: "16:00", count: 1164 },
        { hour: "17:00", count: 1827 }, { hour: "18:00", count: 2606 }
    ],
    daysOfWeek: [
        { day: "Monday", count: 342 }, { day: "Tuesday", count: 567 },
        { day: "Wednesday", count: 489 }, { day: "Thursday", count: 721 },
        { day: "Friday", count: 938 }, { day: "Saturday", count: 1145 },
        { day: "Sunday", count: 870 }
    ],
    mostMessaged: [
        { name: "Alina", count: 5023 },
        { name: "Max", count: 4812 }
    ],
    wordCounts: [
        { name: "Alina", word: "pizza", count: 123 },
        { name: "Max", word: "pizza", count: 42 }
    ],
    participantWordStats: [
        {
            name: "Alina",
            totalWords: 42500,
            topWords: [
                { word: "babe", count: 2371 },
                { word: "love", count: 1850 },
                { word: "okay", count: 1200 },
                { word: "really", count: 980 },
                { word: "yes", count: 850 },
                { word: "no", count: 720 },
                { word: "haha", count: 680 },
                { word: "good", count: 590 },
                { word: "night", count: 510 },
                { word: "sorry", count: 430 }
            ]
        },
        {
            name: "Max",
            totalWords: 39800,
            topWords: [
                { word: "babe", count: 1876 },
                { word: "love", count: 1650 },
                { word: "okay", count: 1100 },
                { word: "yeah", count: 950 },
                { word: "lol", count: 880 },
                { word: "hey", count: 760 },
                { word: "cool", count: 620 },
                { word: "nice", count: 540 },
                { word: "sure", count: 490 },
                { word: "thanks", count: 410 }
            ]
        }
    ],
	mediaCounts : [ {
    name : "STICKER",
    count : 947
	}, {
    name : "PHOTO",
    count : 246
	}, {
    name : "VIDEO",
    count : 38
	}, {
    name : "AUDIO",
    count : 16
	}, {
    name : "GIF",
    count : 15
	} ]
};

let currentData = JSON.parse(JSON.stringify(DEFAULT_DATA));

function formatMostUsedWords(wordsData) {
    if (!Array.isArray(wordsData) || wordsData.length === 0) return [];
    if (typeof wordsData[0] === 'string') {
        return wordsData.map(w => ({ word: w, value: null }));
    } else if (wordsData[0] && typeof wordsData[0] === 'object' && 'word' in wordsData[0]) {
        return wordsData.map(item => ({ word: item.word, value: item.value }));
    }
    return [];
}

function renderDashboard() {
    const container = document.getElementById('renderTarget');
    if (!container) return;
    const d = currentData;

    let totalMessages = 0;
    if (d.mostMessaged && Array.isArray(d.mostMessaged)) {
        totalMessages = d.mostMessaged.reduce((sum, p) => sum + (p.count || 0), 0);
    }
    
    let mediaHtml = '';
    if (d.mediaCounts && Array.isArray(d.mediaCounts) && d.mediaCounts.length > 0) {
        const mediaIcons = {
            audio: '🎤',
            gif: '🎞️',
            photo: '📷',
            sticker: '💟',
            video: '🎬'
        };
        const mediaItems = d.mediaCounts.map(item => {
            const type = item.name.toLowerCase();
            const icon = mediaIcons[type] || '📄';
            return `
                <div class="media-stat">
                    <span>${icon}</span>
                    <span>${item.name}</span>
                    <strong>${item.count.toLocaleString()}</strong>
                </div>
            `;
        }).join('');
        if (mediaItems) {
            mediaHtml = `<div class="media-stats-row">${mediaItems}</div>`;
        }
    }

    const morningMax = Math.max(...d.timeSlotsMorning.map(s => s.count), 1);
    const afternoonMax = Math.max(...d.timeSlotsAfternoon.map(s => s.count), 1);
    const msgMax = Math.max(...d.mostMessaged.map(m => m.count), 1);

    let totalWeeklyMessages = 0;
    if (d.daysOfWeek && Array.isArray(d.daysOfWeek)) {
        totalWeeklyMessages = d.daysOfWeek.reduce((sum, day) => sum + (day.count || 0), 0);
    }
    const dayMax = Math.max(...d.daysOfWeek.map(day => day.count), 1);

    const daysHtml = d.daysOfWeek.map(day => {
        const count = day.count || 0;
        const barPercent = (count / dayMax) * 100;
        let sharePercent = 0;
        if (totalWeeklyMessages > 0) {
            sharePercent = (count / totalWeeklyMessages) * 100;
        }
        const shareFormatted = sharePercent.toFixed(1);
        return `
            <div class="day-item">
                <div class="day-name">${day.day.slice(0,3)}</div>
                <div class="day-bar">
                    <div class="day-fill" style="width: ${barPercent}%;"></div>
                </div>
                <div style="font-size:0.7rem; margin-top:6px; display:flex; justify-content:space-between; gap:0.5rem;">
                    <span>${count.toLocaleString()} msgs</span>
                    <span style="font-weight:600;">${shareFormatted}%</span>
                </div>
            </div>
        `;
    }).join('');

    const morningRows = d.timeSlotsMorning.map(slot => {
        const percent = (slot.count / morningMax) * 100;
        return `<div class="time-row">
                    <div class="time-hour">${slot.hour}</div>
                    <div class="time-bar-bg"><div class="time-bar" style="width: ${percent}%;"></div></div>
                    <div class="time-count">${slot.count}</div>
                </div>`;
    }).join('');
    const afternoonRows = d.timeSlotsAfternoon.map(slot => {
        const percent = (slot.count / afternoonMax) * 100;
        return `<div class="time-row">
                    <div class="time-hour">${slot.hour}</div>
                    <div class="time-bar-bg"><div class="time-bar" style="width: ${percent}%;"></div></div>
                    <div class="time-count">${slot.count}</div>
                </div>`;
    }).join('');

    const emojiHtml = d.topEmojis.map(e => `<div class="emoji-item">${e.emoji} <span style="font-weight:600">${e.value}</span></div>`).join('');

    const wordsFormatted = formatMostUsedWords(d.mostUsedWords);
    const wordsHtml = wordsFormatted.map(w => {
        if (w.value !== null && w.value !== undefined) {
            return `<span class="word-chip">${w.word} <strong>${w.value.toLocaleString()}</strong></span>`;
        } else {
            return `<span class="word-chip">${w.word}</span>`;
        }
    }).join('');

    let participantWordStatsHtml = '';
	if (d.participantWordStats && Array.isArray(d.participantWordStats) && d.participantWordStats.length > 0) {
    const statsColumns = d.participantWordStats.map(stat => {
        const topWordsList = stat.topWords.slice(0, 10).map((w, idx) => 
            `<li style="display: flex; justify-content: space-between; padding: 0.25rem 0; border-bottom: 1px solid var(--border-light);">
                <span>${idx + 1}. ${w.word}</span>
                <strong style="color: var(--btn-primary-hover);">${w.count.toLocaleString()}</strong>
            </li>`
        ).join('');
        
        return `
            <div class="stat-block" style="margin-bottom: 0;">
                <div class="block-title">📝 ${stat.name}'s vocabulary</div>
                <div style="margin-bottom: 0.75rem; font-size: 1rem;">
                    <strong>Total words:</strong> 
                    <span style="font-size: 1.3rem; font-weight: 800; color: var(--btn-primary-hover);">${stat.totalWords.toLocaleString()}</span>
                </div>
                <div style="margin-top: 0.5rem;">
                    <div style="font-weight: 600; margin-bottom: 0.5rem;">🏆 Top 10 most used words</div>
                    <ul style="list-style: none; padding: 0; margin: 0;">
                        ${topWordsList}
                    </ul>
                </div>
            </div>
        `;
    }).join('');
    
    participantWordStatsHtml = `
        <div class="two-col-grid" style="margin-top: 1rem;">
            ${statsColumns}
        </div>
    `;
}

    const recordsHtml = d.records.map(rec => `<div class="record-card"><div class="record-value">${rec.value}</div><div class="record-label">${rec.label}</div></div>`).join('');

    const msgBars = d.mostMessaged.map(p => {
        const percent = (p.count / msgMax) * 100;
        return `<div class="person-stat">
                    <div class="person-name">${p.name}</div>
                    <div class="person-count">${p.count.toLocaleString()}</div>
                    <div style="height:6px; background:var(--person-bar-bg); width:100%; border-radius:6px; margin-top:6px;"><div style="width:${percent}%; background:#6c55b9; height:6px; border-radius:6px;"></div></div>
                </div>`;
    }).join('');

    let battleHtml = '';
    if (d.wordCounts && d.wordCounts.length) {
        battleHtml = `<div class="word-battle" style="margin-top: 1.5rem;">
                        <div class="battle-title">💬 Stats about the word: “${d.wordCounts[0].word}”</div>
                        <div class="battle-row">
                            <span><span class="battle-name">${d.wordCounts[0].name}</span> said <u>${d.wordCounts[0].word}</u> <strong>${d.wordCounts[0].count.toLocaleString()}</strong> times</span>
                            ${d.wordCounts[1] ? `<span><span class="battle-name">${d.wordCounts[1].name}</span> said <u>${d.wordCounts[0].word}</u> <strong>${d.wordCounts[1].count.toLocaleString()}</strong> times</span>` : ''}
                        </div>
                     </div>`;
    }

    let dateHtml = '';
    if (d.chatDates && (d.chatDates.start || d.chatDates.end)) {
        const start = d.chatDates.start || '?';
        const end = d.chatDates.end || '?';
        dateHtml = `
            <div class="chat-period">
                <div class="period-badge">
                    📅 ${start} — ${end} 📅
                </div>
            </div>
        `;
    }

    const fullHtml = `
        <div class="title-section">
            <div class="main-title">${d.title}</div>
            <div class="participant-names">
                <span>✨ ${d.participants.left}</span> <span style="color:#f0bbe7;">&</span> <span>${d.participants.right} ✨</span>
            </div>
        </div>
        ${dateHtml}
        <div class="total-messages-row">
            <div class="total-badge">
                <div class="total-label">Total Messages</div>
                <div class="total-number">${totalMessages.toLocaleString()}</div>
            </div>
        </div>
        ${mediaHtml}

        <div class="stat-block most-messaged-block">
            <div class="block-title">💬 MOST MESSAGED</div>
            <div class="messaged-compare">
                ${msgBars}
            </div>
        </div>

        <div class="reply-row">
            <div class="reply-badge">⏱️ <strong>Average Reply Times</strong></div>
            <div class="reply-stat">${d.participants.left} took <strong>${d.replyTimes.left} min</strong> on average</div>
            <div class="reply-stat">${d.participants.right} took <strong>${d.replyTimes.right} min</strong> on average</div>
        </div>

        <div class="two-col-grid">
            <div class="stat-block">
                <div class="block-title">🏆 TOP EMOJI</div>
                <div class="emoji-list">${emojiHtml}</div>
            </div>
            <div class="stat-block">
                <div class="block-title">📖 MOST USED WORDS</div>
                <div class="words-cloud">${wordsHtml}</div>
            </div>
        </div>

        ${participantWordStatsHtml}

        ${battleHtml}

        <div class="records-grid">
            ${recordsHtml}
        </div>

        <div class="time-group">
            <div class="time-column">
                <div class="time-header">🌙 MORNING · NIGHT OWLS</div>
                ${morningRows}
            </div>
            <div class="time-column">
                <div class="time-header">☀️ AFTERNOON · EVENING</div>
                ${afternoonRows}
            </div>
        </div>

        <div class="stat-block" style="margin-bottom: 1.5rem;">
            <div class="block-title">📅 DAYS OF THE WEEK</div>
            <div class="days-row">
                ${daysHtml}
            </div>
        </div>

        <hr style="opacity:0.4;" />
        <div style="font-size:0.7rem; text-align:center; color:var(--footer-text); margin-top:0.8rem;">✨ chat analytics ✨</div>
    `;

    container.innerHTML = fullHtml;
}

function updateChatData(newData) {
    if (!newData) return;
    currentData = {
        ...currentData,
        ...newData,
        participants: { ...currentData.participants, ...(newData.participants || {}) },
        replyTimes: { ...currentData.replyTimes, ...(newData.replyTimes || {}) },
        topEmojis: newData.topEmojis || currentData.topEmojis,
        mostUsedWords: newData.mostUsedWords || currentData.mostUsedWords,
        records: newData.records || currentData.records,
        timeSlotsMorning: newData.timeSlotsMorning || currentData.timeSlotsMorning,
        timeSlotsAfternoon: newData.timeSlotsAfternoon || currentData.timeSlotsAfternoon,
        daysOfWeek: newData.daysOfWeek || currentData.daysOfWeek,
        mostMessaged: newData.mostMessaged || currentData.mostMessaged,
        wordCounts: newData.wordCounts || currentData.wordCounts,
        title: newData.title || currentData.title,
        chatDates: newData.chatDates ? { ...currentData.chatDates, ...newData.chatDates } : currentData.chatDates,
        participantWordStats: newData.participantWordStats || currentData.participantWordStats,
    };
    renderDashboard();
}

function resetToDefault() {
    currentData = JSON.parse(JSON.stringify(DEFAULT_DATA));
    renderDashboard();
}

async function exportAsImage() {
    const card = document.getElementById('statsCard');
    if (!card) return;
    const originalOverflow = card.style.overflow;
    card.style.overflow = 'visible';
    try {
        const canvas = await html2canvas(card, {
            scale: 2.5,
            backgroundColor: getComputedStyle(document.body).getPropertyValue('--card-bg').trim() || '#ffffff',
            logging: false,
        });
        const link = document.createElement('a');
        link.download = 'chat_summary.png';
        link.href = canvas.toDataURL('image/png');
        link.click();
    } catch (err) {
        console.error(err);
        alert('Could not generate image.');
    } finally {
        card.style.overflow = originalOverflow;
    }
}

function handleJSONFileUpload(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
        try {
            const parsed = JSON.parse(e.target.result);
            updateChatData(parsed);
        } catch (err) {
            alert('Invalid JSON: ' + err.message);
            console.error(err);
        }
    };
    reader.readAsText(file);
}

function initDarkMode() {
    const toggleBtn = document.getElementById('darkModeToggle');
    if (!toggleBtn) return;
    
    const savedMode = localStorage.getItem('whatsappDarkMode');
    const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    const isDark = savedMode === 'true' || (savedMode === null && prefersDark);
    
    if (isDark) {
        document.body.classList.add('dark');
        toggleBtn.innerHTML = '☀️ Light Mode';
    } else {
        document.body.classList.remove('dark');
        toggleBtn.innerHTML = '🌙 Dark Mode';
    }
    
    toggleBtn.addEventListener('click', () => {
        const isDarkNow = document.body.classList.toggle('dark');
        localStorage.setItem('whatsappDarkMode', isDarkNow);
        toggleBtn.innerHTML = isDarkNow ? '☀️ Light Mode' : '🌙 Dark Mode';
    });
}

document.addEventListener('DOMContentLoaded', () => {
    renderDashboard();
    initDarkMode();
    
    document.getElementById('exportImageBtn')?.addEventListener('click', exportAsImage);
    document.getElementById('resetDefaultBtn')?.addEventListener('click', resetToDefault);
    const fileInput = document.getElementById('jsonFileInput');
    if (fileInput) {
        fileInput.addEventListener('change', (event) => {
            if (event.target.files && event.target.files[0]) {
                handleJSONFileUpload(event.target.files[0]);
            }
            fileInput.value = '';
        });
    }
    window.updateChatData = updateChatData;
});
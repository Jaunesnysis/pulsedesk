import { useState, useEffect } from "react";
import "./App.css";

const API = "http://localhost:8080";

function getBadgeClass(category) {
  const classes = {
    bug: "badge-bug",
    feature: "badge-feature",
    billing: "badge-billing",
    account: "badge-account",
    other: "badge-other",
  };
  return classes[category] || "badge-other";
}

function getPriorityClass(priority) {
  const classes = { high: "high", medium: "medium", low: "low" };
  return classes[priority] || "low";
}

function formatDate(dateStr) {
  return new Date(dateStr).toLocaleString();
}

function TicketCard({ ticket }) {
  return (
    <div className="ticket">
      <div className={`priority-bar ${getPriorityClass(ticket.priority)}`} />
      <div>
        <div className="ticket-title">{ticket.title}</div>
        <div className="ticket-summary">{ticket.summary}</div>
        <div className="ticket-meta">
          <span className={`badge ${getBadgeClass(ticket.category)}`}>
            {ticket.category}
          </span>
          {ticket.priority} priority · {formatDate(ticket.createdAt)}
        </div>
      </div>
    </div>
  );
}

function CommentCard({ comment }) {
  return (
    <div className="comment">
      <div>
        <div className="comment-text">{comment.content}</div>
        <div className="comment-meta">
          {comment.source} · {formatDate(comment.createdAt)}
        </div>
      </div>
      {comment.convertedToTicket ? (
        <span className="pill-yes">ticket created</span>
      ) : (
        <span className="pill-no">no ticket</span>
      )}
    </div>
  );
}

export default function App() {
  const [content, setContent] = useState("");
  const [source, setSource] = useState("web-form");
  const [tickets, setTickets] = useState([]);
  const [comments, setComments] = useState([]);
  const [loading, setLoading] = useState(false);
  const [toast, setToast] = useState("");

  const showToast = (msg) => {
    setToast(msg);
    setTimeout(() => setToast(""), 3000);
  };

  const loadData = async () => {
    const [t, c] = await Promise.all([
      fetch(`${API}/tickets`).then((r) => r.json()),
      fetch(`${API}/comments`).then((r) => r.json()),
    ]);
    setTickets(t);
    setComments(c);
  };

  useEffect(() => {
    loadData();
  }, []);

  const submitComment = async () => {
    if (!content.trim() || !source.trim()) {
      showToast("Please fill in both fields");
      return;
    }

    setLoading(true);
    try {
      const res = await fetch(`${API}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ content, source }),
      });

      if (!res.ok) throw new Error();
      const comment = await res.json();
      setContent("");
      showToast(
        comment.convertedToTicket
          ? "Comment submitted — ticket created!"
          : "Comment submitted — no ticket needed",
      );
      await loadData();
    } catch {
      showToast("Error submitting comment");
    } finally {
      setLoading(false);
    }
  };

  const ignored = comments.length - tickets.length;

  return (
    <>
      <nav>
        <div className="nav-dot" />
        <span className="nav-title">PulseDesk</span>
        <span className="nav-sub">Comment Triage System</span>
      </nav>

      <main>
        <div className="card">
          <div className="card-label">Submit feedback</div>
          <div className="field-label">Comment</div>
          <textarea
            value={content}
            onChange={(e) => setContent(e.target.value)}
            placeholder="Describe your issue or feedback..."
          />
          <div className="field-label">Source</div>
          <input
            type="text"
            value={source}
            onChange={(e) => setSource(e.target.value)}
            placeholder="e.g. web-form, app-review, chat"
          />
          <button onClick={submitComment} disabled={loading}>
            {loading ? "Analyzing..." : "Analyze & submit"}
          </button>
        </div>

        <div className="card">
          <div className="card-label">Overview</div>
          <div className="stats">
            <div className="stat">
              <div className="stat-num">{comments.length}</div>
              <div className="stat-label">Comments</div>
            </div>
            <div className="stat">
              <div className="stat-num">{tickets.length}</div>
              <div className="stat-label">Tickets</div>
            </div>
            <div className="stat">
              <div className="stat-num">{ignored}</div>
              <div className="stat-label">Ignored</div>
            </div>
          </div>
          <div className="card-label">Latest ticket</div>
          {tickets.length > 0 ? (
            <TicketCard ticket={tickets[tickets.length - 1]} />
          ) : (
            <div className="empty">No tickets yet</div>
          )}
        </div>

        <div className="card card-full">
          <div className="card-label">Tickets</div>
          {tickets.length > 0 ? (
            tickets.map((t) => <TicketCard key={t.id} ticket={t} />)
          ) : (
            <div className="empty">No tickets yet</div>
          )}
        </div>

        <div className="card card-full">
          <div className="card-label">All comments</div>
          {comments.length > 0 ? (
            [...comments]
              .reverse()
              .map((c) => <CommentCard key={c.id} comment={c} />)
          ) : (
            <div className="empty">No comments yet</div>
          )}
        </div>
      </main>

      {toast && <div className="toast show">{toast}</div>}
    </>
  );
}

import { useState } from "react";
import { Link } from "react-router";
import type { Gym } from "../../types/gym";

export function GymCard({ gym }: { gym: Gym }) {
  const [imageFailed, setImageFailed] = useState(false);
  const showImage = gym.imageUrl && !imageFailed;
  const meta = [gym.comuna, gym.description].filter(Boolean).join(" · ");

  return (
    <article className="flex w-full flex-col overflow-hidden rounded-lg border border-line bg-white">
      <div className="flex aspect-[16/9] items-center justify-center bg-line/70">
        {showImage ? (
          <img
            src={gym.imageUrl!}
            alt={`Foto de ${gym.name}`}
            loading="lazy"
            onError={() => setImageFailed(true)}
            className="size-full object-cover"
          />
        ) : (
          <svg aria-hidden="true" viewBox="0 0 24 24" className="size-7 text-muted" fill="none" stroke="currentColor" strokeWidth="1.6">
            <rect x="3" y="4" width="18" height="16" rx="2" />
            <circle cx="9" cy="10" r="2" />
            <path d="m21 16-5-5-9 9" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        )}
      </div>
      <div className="flex flex-1 flex-col p-4">
        <h2 className="font-display text-xl font-semibold">{gym.name}</h2>
        {meta && (
          <p className="mt-1 flex gap-1.5 text-sm leading-5 text-muted">
            <svg aria-hidden="true" viewBox="0 0 24 24" className="mt-0.5 size-4 shrink-0" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path d="M12 21s-7-6.2-7-12a7 7 0 0 1 14 0c0 5.8-7 12-7 12Z" strokeLinejoin="round" />
              <circle cx="12" cy="9" r="2.5" />
            </svg>
            <span className="line-clamp-2">{meta}</span>
          </p>
        )}
        <div className="mt-auto pt-4">
          <Link
            to={`/gimnasios/${gym.id}`}
            className="flex h-10 w-full items-center justify-center rounded-md bg-ink text-sm font-semibold text-white transition-colors hover:bg-ink/85"
          >
            Ver horarios
          </Link>
        </div>
      </div>
    </article>
  );
}

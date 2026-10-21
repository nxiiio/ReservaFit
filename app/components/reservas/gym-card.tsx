import { useState } from "react";
import { Link } from "react-router";
import type { Gym } from "../../types/gym";
import { ArrowIcon } from "../landing/arrow-icon";
import { ImageIcon } from "../ui/icons";

export function GymCard({ gym }: { gym: Gym }) {
  const [imageFailed, setImageFailed] = useState(false);
  const showImage = gym.imageUrl && !imageFailed;
  const meta = [gym.comuna, gym.description].filter(Boolean).join(" · ");

  return (
    <article className="lift group flex w-full flex-col rounded-xl bg-white">
      <div className="flex aspect-[16/9] items-center justify-center overflow-hidden rounded-t-xl bg-line/70">
        {showImage ? (
          <img
            src={gym.imageUrl!}
            alt={`Foto de ${gym.name}`}
            loading="lazy"
            onError={() => setImageFailed(true)}
            className="size-full object-cover transition-transform duration-500 ease-out-expo motion-safe:group-hover:scale-[1.03]"
          />
        ) : (
          <ImageIcon className="size-7 text-muted" />
        )}
      </div>
      <div className="flex flex-1 flex-col p-5">
        <h2 className="font-display text-2xl leading-tight font-semibold">{gym.name}</h2>
        {meta && (
          <p className="mt-1.5 flex gap-1.5 text-sm leading-5 text-muted">
            <svg aria-hidden="true" viewBox="0 0 24 24" className="mt-0.5 size-4 shrink-0" fill="none" stroke="currentColor" strokeWidth="1.8">
              <path d="M12 21s-7-6.2-7-12a7 7 0 0 1 14 0c0 5.8-7 12-7 12Z" strokeLinejoin="round" />
              <circle cx="12" cy="9" r="2.5" />
            </svg>
            <span className="line-clamp-2">{meta}</span>
          </p>
        )}
        <div className="mt-auto pt-5">
          <Link
            to={`/gimnasios/${gym.id}`}
            className="press flex h-11 w-full items-center justify-between rounded-lg bg-ink px-4 text-sm font-semibold text-paper hover:bg-ink/85"
          >
            Ver horarios <ArrowIcon className="nudge size-4" variant="white" />
          </Link>
        </div>
      </div>
    </article>
  );
}

// Placeholder with the same shape as GymCard while the catalog loads
export function GymCardSkeleton() {
  return (
    <div aria-hidden="true" className="flex w-full flex-col rounded-xl bg-white shadow-card">
      <div className="skeleton aspect-[16/9] rounded-t-xl" />
      <div className="flex flex-1 flex-col p-5">
        <div className="skeleton h-6 w-2/3 rounded-md" />
        <div className="skeleton mt-3 h-4 w-1/2 rounded-md" />
        <div className="skeleton mt-8 h-11 rounded-lg" />
      </div>
    </div>
  );
}

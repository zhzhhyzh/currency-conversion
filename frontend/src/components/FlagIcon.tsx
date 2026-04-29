import React from 'react';
import * as Flags from 'country-flag-icons/react/3x2';

interface FlagIconProps {
  countryCode: string;
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const sizeClasses = {
  sm: 'flag-size-sm',
  md: 'flag-size-md',
  lg: 'flag-size-lg',
};

export const FlagIcon: React.FC<FlagIconProps> = ({ 
  countryCode, 
  size = 'md', 
  className = '' 
}) => {
  const code = countryCode.toUpperCase();
  
  const Flag = Flags[code as keyof typeof Flags] as React.ComponentType<{ className?: string }> | undefined;

  if (!Flag) {
    return (
      <div 
        className={`${sizeClasses[size]} flag-icon flag-fallback ${className}`}
      >
        {countryCode.substring(0, 2).toUpperCase()}
      </div>
    );
  }

  return (
    <span className={`${sizeClasses[size]} flag-icon flag-icon-circle ${className}`}>
      <Flag className="flag-svg" />
    </span>
  );
};

export default FlagIcon;
